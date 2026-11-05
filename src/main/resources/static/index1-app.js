const state = {
  token: localStorage.getItem('carManualToken') || '',
  username: localStorage.getItem('carManualUsername') || '',
  userId: Number(localStorage.getItem('carManualUserId')) || null
};

const loginModal = document.getElementById('loginModal');
const loginForm = document.getElementById('loginForm');
const loginError = document.getElementById('loginError');
const loginLoader = document.getElementById('loginLoader');
const userMenu = document.getElementById('userMenu');
const userName = document.getElementById('userName');
const dashboardUserName = document.getElementById('dashboardUserName');
const dashboard = document.getElementById('dashboard');
const loginBtn = document.getElementById('loginBtn');
const taskList = document.getElementById('taskList');
const taskDescription = document.getElementById('taskDescription');
const taskStatus = document.getElementById('taskStatus');
const taskError = document.getElementById('taskError');
const modalTitle = document.getElementById('modalTitle');
const submitBtn = document.getElementById('submitBtn');
const toggleAuthMode = document.getElementById('toggleAuthMode');
let authMode = 'login';

function openLoginModal() {
  loginModal.classList.add('active');
}

function closeLoginModal() {
  loginModal.classList.remove('active');
}

function scrollToSection(id) {
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' });
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function setStatus(message, isError = false) {
  taskStatus.textContent = message;
  taskStatus.classList.toggle('hidden', !message || isError);
  taskError.textContent = isError ? message : '';
  taskError.classList.toggle('hidden', !isError);
}

function showAuthState() {
  if (state.token && state.username) {
    userMenu.classList.add('active');
    userName.textContent = state.username;
    dashboardUserName.textContent = state.username;
    dashboard.classList.add('active');
    loginBtn.style.display = 'none';
    loadTasks();
  } else {
    userMenu.classList.remove('active');
    userName.textContent = '';
    dashboardUserName.textContent = '-';
    dashboard.classList.remove('active');
    loginBtn.style.display = 'inline-block';
    taskList.innerHTML = '';
  }
}

function persistAuth() {
  if (state.token) {
    localStorage.setItem('carManualToken', state.token);
  } else {
    localStorage.removeItem('carManualToken');
  }
  if (state.username) {
    localStorage.setItem('carManualUsername', state.username);
  } else {
    localStorage.removeItem('carManualUsername');
  }
  if (state.userId) {
    localStorage.setItem('carManualUserId', String(state.userId));
  } else {
    localStorage.removeItem('carManualUserId');
  }
}

function authHeaders() {
  return {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${state.token}`
  };
}

function setAuthMode(mode) {
  authMode = mode;
  modalTitle.textContent = mode === 'login' ? 'Entrar' : 'Criar conta';
  submitBtn.textContent = mode === 'login' ? 'Entrar' : 'Cadastrar';
  toggleAuthMode.textContent = mode === 'login' ? 'Não tem uma conta? Cadastre-se' : 'Já tem uma conta? Entrar';
  document.getElementById('password').minLength = mode === 'register' ? 8 : 1;
  loginError.style.display = 'none';
}

function renderTasks(tasks) {
  taskList.innerHTML = '';

  if (!tasks || tasks.length === 0) {
    taskList.innerHTML = '<li class="task-item"><span>Você ainda não possui tarefas.</span></li>';
    return;
  }

  tasks.forEach(task => {
    const li = document.createElement('li');
    li.className = 'task-item';
    li.innerHTML = `
      <div>
        <strong>#${task.id}</strong>
        <p>${task.description}</p>
      </div>
      <div class="task-actions">
        <button class="secondary-btn" data-action="edit" data-id="${task.id}">Editar</button>
        <button class="danger-btn" data-action="delete" data-id="${task.id}">Excluir</button>
      </div>
    `;
    taskList.appendChild(li);
  });

  taskList.querySelectorAll('button').forEach(button => {
    button.addEventListener('click', async () => {
      const { action, id } = button.dataset;
      if (action === 'delete') {
        await deleteTask(Number(id));
      }
      if (action === 'edit') {
        const task = tasks.find(item => item.id === Number(id));
        if (!task) return;

        const nextDescription = prompt('Editar descrição da tarefa:', task.description);
        if (nextDescription === null) return;
        if (!nextDescription.trim()) {
          setStatus('A descrição não pode ficar vazia.', true);
          return;
        }

        await updateTask(Number(id), nextDescription.trim());
      }
    });
  });
}

async function apiFetch(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: {
      ...(options.headers || {}),
      ...(options.headers && options.headers.Authorization ? {} : {})
    }
  });

  const contentType = response.headers.get('content-type') || '';
  const payload = contentType.includes('application/json') ? await response.json() : await response.text();

  if (!response.ok) {
    const message = typeof payload === 'object' && payload !== null ? (payload.message || 'Erro na requisição.') : payload || 'Erro na requisição.';
    throw new Error(message);
  }

  return payload;
}

async function login(username, password) {
  const response = await fetch('/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });

  const payload = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(payload.message || 'Credenciais inválidas.');
  }

  state.token = payload.token;
  state.username = username;
  persistAuth();

  const meResponse = await fetch('/user/me', {
    headers: { Authorization: `Bearer ${state.token}` }
  });

  if (!meResponse.ok) {
    throw new Error('Não foi possível buscar os dados do usuário.');
  }

  const me = await meResponse.json();
  state.userId = me.id;
  persistAuth();
  showAuthState();
}

async function loadTasks() {
  if (!state.userId || !state.token) return;

  try {
    const tasks = await apiFetch(`/task/user/${state.userId}`, {
      headers: authHeaders()
    });
    renderTasks(tasks);
    setStatus('');
  } catch (error) {
    setStatus(error.message, true);
  }
}

async function createTask() {
  const description = taskDescription.value.trim();
  if (!description) {
    setStatus('Digite uma descrição para a tarefa.', true);
    return;
  }

  try {
    await apiFetch('/task', {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify({ description, user: { id: state.userId } })
    });

    taskDescription.value = '';
    setStatus('Tarefa criada com sucesso!');
    await loadTasks();
  } catch (error) {
    setStatus(error.message, true);
  }
}

async function updateTask(id, description) {
  try {
    await apiFetch(`/task/${id}`, {
      method: 'PUT',
      headers: authHeaders(),
      body: JSON.stringify({ id, description, user: { id: state.userId } })
    });
    setStatus('Tarefa atualizada com sucesso!');
    await loadTasks();
  } catch (error) {
    setStatus(error.message, true);
  }
}

async function deleteTask(id) {
  try {
    await apiFetch(`/task/${id}`, {
      method: 'DELETE',
      headers: authHeaders()
    });
    setStatus('Tarefa removida com sucesso!');
    await loadTasks();
  } catch (error) {
    setStatus(error.message, true);
  }
}

loginForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  const username = document.getElementById('username').value.trim();
  const password = document.getElementById('password').value.trim();

  if (!username || !password) {
    loginError.textContent = 'Usuário e senha são obrigatórios.';
    loginError.style.display = 'block';
    return;
  }

  if (authMode === 'register' && password.length < 8) {
    loginError.textContent = 'A senha deve ter pelo menos 8 caracteres.';
    loginError.style.display = 'block';
    return;
  }

  loginLoader.style.display = 'block';
  loginError.style.display = 'none';

  try {
    if (authMode === 'register') {
      await apiFetch('/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });
      setAuthMode('login');
      document.getElementById('username').value = username;
      document.getElementById('password').value = '';
      loginError.textContent = 'Cadastro realizado. Entre com seu usuário e senha.';
      loginError.style.color = '#1f7a45';
      loginError.style.display = 'block';
    } else {
      await login(username, password);
      closeLoginModal();
      loginForm.reset();
    }
  } catch (error) {
    loginError.style.color = '#e74c3c';
    loginError.textContent = error.message;
    loginError.style.display = 'block';
  } finally {
    loginLoader.style.display = 'none';
  }
});

toggleAuthMode.addEventListener('click', () => {
  setAuthMode(authMode === 'login' ? 'register' : 'login');
});

document.getElementById('createTaskBtn').addEventListener('click', createTask);

document.addEventListener('click', (event) => {
  if (event.target === loginModal) {
    closeLoginModal();
  }
});

function logout() {
  state.token = '';
  state.username = '';
  state.userId = null;
  persistAuth();
  showAuthState();
}

showAuthState();
if (state.token && state.username) {
  const usernameInput = document.getElementById('username');
  if (usernameInput) usernameInput.value = state.username;
}
