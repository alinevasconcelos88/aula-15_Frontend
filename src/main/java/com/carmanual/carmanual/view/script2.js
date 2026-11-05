
const API_URL = "http://localhost:8080/task";
const USER_ID = 1;

// Carregar tarefas ao iniciar
document.addEventListener("DOMContentLoaded", () => {
  loadTasks();
});

// Enviar formulário
document.getElementById("taskForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const taskId = document.getElementById("taskId").value;
  const description = document.getElementById("description").value.trim();

  const task = { description, user: { id: USER_ID } };

  try {
    let response;
    if (taskId) {
      // Atualizar
      response = await fetch(`${API_URL}/${taskId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(task)
      });
    } else {
      // Criar
      response = await fetch(API_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(task)
      });
    }
    if (!response.ok) {
      throw new Error(`API retornou ${response.status}: ${response.statusText}`);
    }
    resetForm();
    loadTasks();
  } catch (error) {
    alert(`Erro ao salvar tarefa: ${error.message}`);
    console.error(error);
  }
});

// Cancelar edição
document.getElementById("btnCancel").addEventListener("click", resetForm);

// Carregar tarefas
async function loadTasks() {
  showLoading(true);
  try {
    const res = await fetch(`http://localhost:8080/task/user/${USER_ID}`);
    if (!res.ok) {
      throw new Error(`API retornou ${res.status}: ${res.statusText}`);
    }
    const tasks = await res.json();
    renderTasks(tasks);
  } catch (error) {
    console.error("Erro ao carregar tarefas:", error);
    document.getElementById("tasksList").innerHTML = "<tr><td colspan='3' class='text-center'>Erro ao carregar</td></tr>";
  } finally {
    showLoading(false);
  }
}

// Renderizar tabela
function renderTasks(tasks) {
  const tbody = document.getElementById("tasksList");
  if (tasks.length === 0) {
    tbody.innerHTML = `<tr><td colspan="3" class="text-center">Nenhuma tarefa cadastrada</td></tr>`;
    return;
  }

  tbody.innerHTML = tasks.map(t => `
    <tr>
      <td>${t.id}</td>
      <td>${t.description}</td>
      <td>
        <button class="btn btn-sm btn-warning me-1" onclick="editTask(${t.id}, '${t.description.replace(/'/g, "\\'")}')">Editar</button>
        <button class="btn btn-sm btn-danger" onclick="deleteTask(${t.id})">Excluir</button>
      </td>
    </tr>
  `).join("");
}

// Editar tarefa
function editTask(id, description) {
  document.getElementById("taskId").value = id;
  document.getElementById("description").value = description;
  document.getElementById("btnCancel").style.display = "inline-block";
}

// Excluir tarefa
async function deleteTask(id) {
  if (!confirm("Tem certeza que deseja excluir esta tarefa?")) return;
  try {
    const response = await fetch(`${API_URL}/${id}`, { method: "DELETE" });
    if (!response.ok) {
      throw new Error(`API retornou ${response.status}: ${response.statusText}`);
    }
    loadTasks();
  } catch (error) {
    alert(`Erro ao excluir tarefa: ${error.message}`);
    console.error(error);
  }
}

// Resetar formulário
function resetForm() {
  document.getElementById("taskForm").reset();
  document.getElementById("taskId").value = "";
  document.getElementById("btnCancel").style.display = "none";
}

// Mostrar/ocultar loading
function showLoading(show) {
  document.getElementById("loading").style.display = show ? "block" : "none";
}

