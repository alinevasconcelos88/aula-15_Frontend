const index3Auth = {
  token: localStorage.getItem('carManualToken') || '',
  authUser: null,
  profile: null,
  profileType: 'usuario',
  mechanics: [],
  manuals: [],
  diagnoses: []
};

function apiMessage(payload, fallback) {
  if (payload && typeof payload === 'object') {
    return payload.message || payload.detail || payload.error || fallback;
  }
  return typeof payload === 'string' && payload.trim() ? payload : fallback;
}

async function apiRequest(path, options = {}) {
  const headers = { ...(options.headers || {}) };
  if (options.body && !headers['Content-Type']) headers['Content-Type'] = 'application/json';
  if (index3Auth.token && options.auth !== false) {
    headers.Authorization = `Bearer ${index3Auth.token}`;
  }

  const response = await fetch(path, { ...options, headers });
  const text = await response.text();
  let payload = null;
  if (text) {
    try { payload = JSON.parse(text); } catch { payload = text; }
  }
  if (!response.ok) throw new Error(apiMessage(payload, `Erro HTTP ${response.status}`));
  return payload;
}

function escapeApiHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, character => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  })[character]);
}

function apiToast(message, type = '') {
  if (typeof showToast === 'function') showToast(message, type);
}

function setLoginMessage(message) {
  const error = document.getElementById('loginError');
  error.textContent = message;
  error.style.display = message ? 'block' : 'none';
}

async function resolveDomainProfile(authUser, preferredType) {
  const email = authUser.username;
  if (String(authUser.role || '').toUpperCase() === 'ADMIN') {
    return { profile: authUser, type: 'admin' };
  }

  const [clients, mechanics] = await Promise.all([
    apiRequest('/cliente'),
    apiRequest('/mecanico')
  ]);
  const mechanic = mechanics.find(item => item.email?.toLowerCase() === email.toLowerCase());
  if (mechanic) return { profile: mechanic, type: 'mecanico' };

  let client = clients.find(item => item.email?.toLowerCase() === email.toLowerCase());
  if (!client && preferredType === 'mecanico') {
    const created = await apiRequest('/mecanico', {
      method: 'POST',
      auth: false,
      body: JSON.stringify({ nome: email, email, especialidade: 'Não informada' })
    });
    return { profile: created, type: 'mecanico' };
  }
  if (!client) {
    client = await apiRequest('/cliente', {
      method: 'POST',
      auth: false,
      body: JSON.stringify({ nome: email, email, telefone: '' })
    });
  }
  return { profile: client, type: 'usuario' };
}

function enterIndex3(authUser, profile, profileType) {
  index3Auth.authUser = authUser;
  index3Auth.profile = profile;
  index3Auth.profileType = profileType;
  usuarioLogado = {
    ...profile,
    authId: authUser.id,
    nome: profile.nome || authUser.username,
    email: profile.email || authUser.username,
    tipo: profileType,
    telefone: profile.telefone || ''
  };
  usuarios = [usuarioLogado];
  localStorage.setItem('carManualToken', index3Auth.token);
  localStorage.setItem('carManualUsername', authUser.username);
  localStorage.setItem('carManualUserId', String(authUser.id));

  document.getElementById('loginScreen').classList.add('hidden');
  document.getElementById('cadastroScreen').classList.add('hidden');
  document.getElementById('mainContent').classList.remove('hidden');
  document.getElementById('headerUser').textContent = `👤 ${usuarioLogado.nome} (${profileType})`;
  document.getElementById('displayNome').textContent = usuarioLogado.nome;
  document.getElementById('displayEmail').textContent = usuarioLogado.email;
  document.getElementById('perfilNome').value = usuarioLogado.nome;
  document.getElementById('perfilEmail').value = usuarioLogado.email;
  document.getElementById('perfilTelefone').value = usuarioLogado.telefone;

  const isAdmin = profileType === 'admin';
  const isMechanic = profileType === 'mecanico';
  document.getElementById('adminDashboard').classList.toggle('hidden', !isAdmin);
  document.getElementById('usuarioDashboard').classList.toggle('hidden', isAdmin || isMechanic);
  document.getElementById('mecanicoDashboard').classList.toggle('hidden', !isMechanic);
  document.getElementById('navIA').style.display = isAdmin ? 'none' : 'block';
  document.getElementById('navHistorico').style.display = !isAdmin && !isMechanic ? 'block' : 'none';
  document.getElementById('navMecanico').style.display = isAdmin ? 'none' : 'block';
  document.getElementById('mecanicoStatusPerfil').classList.toggle('hidden', !isMechanic);
  document.getElementById('pagamentoMensal').classList.add('hidden');
  document.getElementById('manualStatusMsg').textContent = 'A API registra título e descrição; envio de arquivos ainda não está disponível.';

  carregarDadosUsuario();
  apiToast('Conectado à API. Recursos sem endpoint serão informados ao tentar usá-los.', 'success');
}

window.fazerLogin = async function fazerLoginApi() {
  const email = document.getElementById('loginEmail').value.trim();
  const senha = document.getElementById('loginSenha').value;
  if (!email || !senha) return setLoginMessage('Informe e-mail e senha.');

  try {
    setLoginMessage('');
    const auth = await apiRequest('/login', {
      method: 'POST', auth: false,
      body: JSON.stringify({ username: email, password: senha })
    });
    index3Auth.token = auth.token;
    const me = await apiRequest('/user/me');
    const { profile, type } = await resolveDomainProfile(me);
    enterIndex3(me, profile, type);
  } catch (error) {
    index3Auth.token = '';
    localStorage.removeItem('carManualToken');
    setLoginMessage(error.message || 'Não foi possível entrar.');
    apiToast(error.message || 'Falha ao entrar.', 'error');
  }
};

window.fazerCadastro = async function fazerCadastroApi() {
  const name = document.getElementById('cadNome').value.trim();
  const email = document.getElementById('cadEmail').value.trim();
  const password = document.getElementById('cadSenha').value;
  const confirmation = document.getElementById('cadSenhaConfirm').value;
  const type = document.getElementById('cadTipo').value;
  if (!name || !email || !password || !confirmation) return apiToast('Preencha os campos obrigatórios.', 'error');
  if (password !== confirmation) return apiToast('As senhas não coincidem.', 'error');
  if (password.length < 8) return apiToast('A senha deve ter pelo menos 8 caracteres.', 'error');

  try {
    await apiRequest('/auth/register', {
      method: 'POST', auth: false,
      body: JSON.stringify({ username: email, password })
    });
    if (type === 'mecanico') {
      await apiRequest('/mecanico', {
        method: 'POST', auth: false,
        body: JSON.stringify({ nome: name, email, especialidade: 'Não informada' })
      });
    } else {
      await apiRequest('/cliente', {
        method: 'POST', auth: false,
        body: JSON.stringify({ nome: name, email, telefone: '' })
      });
    }
    document.getElementById('loginEmail').value = email;
    document.getElementById('loginSenha').value = '';
    mostrarLogin();
    apiToast('Conta criada pela API. Entre com seu e-mail e senha.', 'success');
  } catch (error) {
    apiToast(error.message || 'Falha no cadastro.', 'error');
  }
};

window.fazerLogout = function fazerLogoutApi() {
  index3Auth.token = '';
  index3Auth.authUser = null;
  index3Auth.profile = null;
  localStorage.removeItem('carManualToken');
  localStorage.removeItem('carManualUsername');
  localStorage.removeItem('carManualUserId');
  usuarioLogado = null;
  document.getElementById('mainContent').classList.add('hidden');
  document.getElementById('loginScreen').classList.remove('hidden');
  setLoginMessage('');
};

window.carregarDadosUsuario = async function carregarDadosUsuarioApi() {
  if (!usuarioLogado || usuarioLogado.tipo === 'admin') return;
  try {
    await Promise.all([carregarDiagnosticosApi(), carregarManuaisApi(), carregarMecanicosApi(), carregarAvaliacoesApi()]);
  } catch (error) {
    apiToast(error.message || 'Falha ao carregar dados da API.', 'error');
  }
};
window.carregarDadosMecanico = window.carregarDadosUsuario;

async function carregarDiagnosticosApi() {
  if (!usuarioLogado?.id) return;
  index3Auth.diagnoses = await apiRequest(`/cliente/${usuarioLogado.id}/diagnosticos`);
  const html = index3Auth.diagnoses.length
    ? index3Auth.diagnoses.slice().reverse().map(item => `<div class="diagnostico-result"><strong>${escapeApiHtml(item.descricao)}</strong><p>${escapeApiHtml(item.resultado)}</p></div>`).join('')
    : '<div class="empty">Nenhum registro de diagnóstico.</div>';
  document.getElementById('historicoDiagnosticos').innerHTML = html;
  document.getElementById('ultimoDiagnostico').innerHTML = html === '<div class="empty">Nenhum registro de diagnóstico.</div>' ? html : html.split('</div>')[0] + '</div>';
}

window.diagnosticarTexto = async function diagnosticarTextoApi() {
  const description = document.getElementById('descricaoProblema').value.trim();
  if (!description) return apiToast('Descreva o problema.', 'error');
  if (!usuarioLogado?.id || usuarioLogado.tipo === 'admin') return apiToast('Entre com uma conta de cliente para registrar o diagnóstico.', 'error');
  const vehicleKey = `carManualVehicleRef:${index3Auth.authUser.id}`;
  let vehicleRef = Number(localStorage.getItem(vehicleKey));
  if (!Number.isSafeInteger(vehicleRef) || vehicleRef <= 0) {
    vehicleRef = Date.now();
    localStorage.setItem(vehicleKey, String(vehicleRef));
  }
  const result = 'Relato registrado pela API. O backend atual ainda não oferece diagnóstico automático por IA.';
  try {
    await apiRequest(`/diagnostico?idCliente=${usuarioLogado.id}`, {
      method: 'POST',
      body: JSON.stringify({ descricao: description, resultado: result, idVeic: vehicleRef })
    });
    document.getElementById('resultadoTexto').innerHTML = `<div class="diagnostico-result"><strong>Relato registrado</strong><p>${escapeApiHtml(result)}</p><p>${escapeApiHtml(description)}</p><small>ID local do veículo: ${vehicleRef}</small></div>`;
    document.getElementById('descricaoProblema').value = '';
    await carregarDiagnosticosApi();
    apiToast('Relato salvo no backend.', 'success');
  } catch (error) {
    apiToast(error.message || 'Não foi possível registrar o diagnóstico.', 'error');
  }
};

window.diagnosticarFoto = function diagnosticarFotoApi() {
  apiToast('A API ainda não possui endpoint para análise de imagens.', 'error');
};

async function carregarManuaisApi() {
  index3Auth.manuals = await apiRequest('/manual');
  const html = index3Auth.manuals.length
    ? index3Auth.manuals.map(item => `<div class="manual-item"><h4>${escapeApiHtml(item.titulo || 'Manual')}</h4><p>${escapeApiHtml(item.descricao || '')}</p></div>`).join('')
    : '<div class="empty">Nenhum manual cadastrado no backend.</div>';
  document.getElementById('manuaisDisponiveis').innerHTML = html;
  document.getElementById('meusManuais').innerHTML = '<div class="empty">A API ainda não identifica o proprietário dos manuais. Consulte a lista geral.</div>';
}

window.cadastrarManual = async function cadastrarManualApi() {
  const title = document.getElementById('manualTitulo').value.trim();
  const description = document.getElementById('manualDesc').value.trim();
  if (!title) return apiToast('Informe o título do manual.', 'error');
  try {
    await apiRequest('/manual', {
      method: 'POST',
      body: JSON.stringify({ titulo: title, descricao: description })
    });
    document.getElementById('manualTitulo').value = '';
    document.getElementById('manualDesc').value = '';
    await carregarManuaisApi();
    apiToast('Manual cadastrado no backend. O arquivo não foi enviado porque a API não oferece upload.', 'success');
  } catch (error) {
    apiToast(error.message || 'Não foi possível cadastrar o manual.', 'error');
  }
};

async function carregarMecanicosApi() {
  index3Auth.mechanics = await apiRequest('/mecanico');
  const options = '<option value="">-- Selecione um mecânico --</option>' + index3Auth.mechanics.map(item =>
    `<option value="${Number(item.id)}">${escapeApiHtml(item.nome)}${item.especialidade ? ` - ${escapeApiHtml(item.especialidade)}` : ''}</option>`
  ).join('');
  ['mecanicoSelect', 'avaliarMecanicoSelect'].forEach(id => {
    const select = document.getElementById(id);
    if (select) select.innerHTML = options;
  });
}

async function carregarAvaliacoesApi() {
  const evaluations = await apiRequest('/avaliacao');
  const container = document.getElementById('avaliacoesRecebidas');
  if (!container) return;
  const html = evaluations.length
    ? evaluations.map(item => `<div class="relatorio-item">${'⭐'.repeat(Number(item.nota) || 0)} ${escapeApiHtml(item.comentario || '')}</div>`).join('')
    : '<div class="empty">Nenhuma avaliação registrada.</div>';
  container.innerHTML = html;
}

window.avaliarMecanico = async function avaliarMecanicoApi() {
  const mechanicId = Number(document.getElementById('avaliarMecanicoSelect').value);
  const note = Number(ratingSelecionado);
  const comment = document.getElementById('avaliacaoComentario').value.trim();
  if (!usuarioLogado?.id || usuarioLogado.tipo !== 'usuario') return apiToast('A avaliação está disponível para contas de cliente.', 'error');
  if (!mechanicId || note < 1 || note > 5) return apiToast('Selecione um mecânico e uma nota de 1 a 5.', 'error');
  try {
    await apiRequest(`/avaliacao?clienteId=${usuarioLogado.id}&mecanicoId=${mechanicId}`, {
      method: 'POST',
      body: JSON.stringify({ nota: note, comentario: comment })
    });
    document.getElementById('avaliacaoComentario').value = '';
    ratingSelecionado = 0;
    await carregarAvaliacoesApi();
    apiToast('Avaliação registrada no backend.', 'success');
  } catch (error) {
    apiToast(error.message || 'Não foi possível enviar a avaliação.', 'error');
  }
};

window.salvarPerfil = async function salvarPerfilApi() {
  const name = document.getElementById('perfilNome').value.trim();
  const email = document.getElementById('perfilEmail').value.trim();
  const phone = document.getElementById('perfilTelefone').value.trim();
  if (!name || !email) return apiToast('Nome e e-mail são obrigatórios.', 'error');
  if (document.getElementById('perfilSenha').value) return apiToast('A API ainda não possui endpoint para alterar senha.', 'error');
  const path = usuarioLogado.tipo === 'mecanico' ? `/mecanico/${usuarioLogado.id}` : `/cliente/${usuarioLogado.id}`;
  const profile = usuarioLogado.tipo === 'mecanico'
    ? { ...index3Auth.profile, nome: name, email }
    : { ...index3Auth.profile, nome: name, email, telefone: phone };
  try {
    index3Auth.profile = await apiRequest(path, { method: 'PUT', body: JSON.stringify(profile) });
    enterIndex3(index3Auth.authUser, index3Auth.profile, index3Auth.profileType);
    apiToast('Perfil atualizado no backend.', 'success');
  } catch (error) {
    apiToast(error.message || 'Não foi possível atualizar o perfil.', 'error');
  }
};

window.abrirRecuperarSenha = function recuperarSenhaIndisponivel() {
  apiToast('A API ainda não oferece recuperação de senha.', 'error');
};
window.recuperarSenha = window.abrirRecuperarSenha;

const apiUnavailableActions = [
  'salvarVeiculo', 'salvarAgenda', 'agendarMecanico', 'abrirPagamento', 'confirmarPagamento',
  'criarAnuncio', 'salvarHistorico', 'configurarAlertas', 'enviarCertificadoPerfil',
  'enviarCertificado', 'aprovarCertificado', 'confirmarRecusa'
];
apiUnavailableActions.forEach(action => {
  window[action] = function unavailableBackendAction() {
    apiToast('Esse recurso ainda não possui endpoint no backend.', 'error');
  };
});

const manualUpload = document.getElementById('manualPdfInput');
manualUpload?.closest('.file-upload-wrapper')?.classList.add('hidden');
const manualCertificate = document.getElementById('certificadoInput');
manualCertificate?.closest('.file-upload-wrapper')?.classList.add('hidden');
const profilePassword = document.getElementById('perfilSenha');
if (profilePassword) {
  profilePassword.disabled = true;
  profilePassword.placeholder = 'Endpoint de alteração de senha indisponível';
}

async function restoreIndex3Session() {
  if (!index3Auth.token) return;
  try {
    const me = await apiRequest('/user/me');
    const { profile, type } = await resolveDomainProfile(me);
    enterIndex3(me, profile, type);
  } catch {
    index3Auth.token = '';
    localStorage.removeItem('carManualToken');
  }
}

document.addEventListener('DOMContentLoaded', restoreIndex3Session);
