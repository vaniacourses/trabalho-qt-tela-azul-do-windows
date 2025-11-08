<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="br.uff.ic.grupo6.banco.model.Cliente"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<!DOCTYPE html>
<html lang="pt-BR" data-bs-theme="dark">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Meus Dados - Banco ATM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="style.css" rel="stylesheet">
    </head>

    <body>
        <%-- Bloco de código Java (Scriptlet) para preparar os dados antes de renderizar a página --%>
        <%
            // Tenta pegar o objeto 'cliente' que foi enviado pelo 'DadosCadastraisServlet'
            Cliente cliente = (Cliente) request.getAttribute("cliente");

            // Se o 'cliente' não veio pela requisição (ex: usuário atualizou a página),
            // tenta pegar da sessão para evitar erros.
            if (cliente == null) {
                // se não houver cliente na requisição, busca na sessão
                if (session.getAttribute("usuarioLogado") instanceof Cliente) {
                    cliente = (Cliente) session.getAttribute("usuarioLogado");
                } else {
                    // Se não há cliente em nenhum lugar, o acesso é inválido. Redireciona para o login.
                    response.sendRedirect("login.jsp");
                    return; // Interrompe a execução da página
                }
            }
        %>

        <div class="d-flex">
            <nav class="sidebar bg-dark-subtle p-3">
                <h2 class="text-white mb-4"><i class="bi bi-coin text-warning"></i> Banco ATM</h2>
                <ul class="nav flex-column">
                    <li class="nav-item mb-2">
                        <a class="nav-link" href="dashboard.jsp"><i class="bi bi-house-door-fill me-2"></i> Início</a>
                    </li>
                    <li class="nav-item mb-2">
                        <a class="nav-link active" href="PerfilServlet"><i class="bi bi-person-circle me-2"></i> Meu Perfil</a>
                    </li>
                </ul>
                <div class="mt-auto">
                    <a href="LogoutServlet" class="btn btn-outline-secondary w-100">
                        <i class="bi bi-box-arrow-right me-2"></i> Sair
                    </a>
                </div>
            </nav>

            <main class="main-content">
                <div class="container-lg py-4 px-md-5">
                    <header class="d-flex justify-content-between align-items-center mb-4">
                        <h3>Meus Dados Cadastrais</h3>
                        <a href="PerfilServlet" class="btn btn-outline-secondary rounded-circle p-2 lh-1" title="Voltar para Meu Perfil">
                            <i class="bi bi-x-lg"></i>
                        </a>
                    </header>

                    <%-- verifica se existe um parâmetro de erro na URL --%>
                    <%
                        String msgErro = request.getParameter("erro");
                        if (msgErro != null && !msgErro.isEmpty()) {
                    %>
                    <div class="alert alert-danger" role="alert">
                        <%= msgErro%>
                    </div>
                    <%
                        }
                    %>

                    <div class="card card-balance p-4">
                        <form action="AtualizarDadosServlet" method="post">

                            <fieldset id="formDados" disabled>
                                <h5 class="mb-3">Dados Pessoais</h5>
                                <div class="row g-3">
                                    <div class="col-12">
                                        <label class="form-label">Nome Completo</label>
                                        <input type="text" class="form-control" name="nome" value="<%= cliente.getNome()%>">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">CPF</label>
                                        <input type="text" class="form-control" name="cpf" value="<%= cliente.getCpf()%>" readonly>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Data de Nascimento</label>
                                        <input type="date" class="form-control" name="dataNascimento" value="<%= cliente.getDataNascimento() != null ? cliente.getDataNascimento().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : ""%>">
                                    </div>
                                </div>

                                <h5 class="mb-3 mt-4">Informações de Contato</h5>
                                <div class="row g-3">
                                    <div class="col-md-7">
                                        <label class="form-label">E-mail</label>
                                        <input type="email" class="form-control" name="email" value="<%= cliente.getEmail() != null ? cliente.getEmail() : ""%>">
                                    </div>
                                    <div class="col-md-5">
                                        <label class="form-label">Telefone</label>
                                        <input type="tel" class="form-control" name="telefone" value="<%= cliente.getTelefone() != null ? cliente.getTelefone() : ""%>">
                                    </div>
                                </div>

                                <h5 class="mb-3 mt-4">Endereço</h5>
                                <div class="row g-3">
                                    <div class="col-md-4"><label class="form-label">CEP</label><input type="text" class="form-control" name="cep" value="<%= cliente.getCep() != null ? cliente.getCep() : ""%>"></div>
                                    <div class="col-md-8"><label class="form-label">Endereço</label><input type="text" class="form-control" name="endereco" value="<%= cliente.getEndereco() != null ? cliente.getEndereco() : ""%>"></div>
                                </div>
                                <div class="row g-3 mt-1">
                                    <div class="col-md-5"><label class="form-label">Bairro</label><input type="text" class="form-control" name="bairro" value="<%= cliente.getBairro() != null ? cliente.getBairro() : ""%>"></div>
                                    <div class="col-md-4"><label class="form-label">Cidade</label><input type="text" class="form-control" name="cidade" value="<%= cliente.getCidade() != null ? cliente.getCidade() : ""%>"></div>
                                    <div class="col-md-3">
                                        <label class="form-label">Estado</label>
                                        <select class="form-select" name="estado">
                                            <%-- Este bloco Java verifica o estado atual do cliente e marca a <option> correta como "selected" --%>
                                            <% String estadoCliente = cliente.getEstado() != null ? cliente.getEstado() : "";%>
                                            <option value="AC" <%= "AC".equals(estadoCliente) ? "selected" : ""%>>AC</option>
                                            <option value="AL" <%= "AL".equals(estadoCliente) ? "selected" : ""%>>AL</option>
                                            <option value="AP" <%= "AP".equals(estadoCliente) ? "selected" : ""%>>AP</option>
                                            <option value="AM" <%= "AM".equals(estadoCliente) ? "selected" : ""%>>AM</option>
                                            <option value="BA" <%= "BA".equals(estadoCliente) ? "selected" : ""%>>BA</option>
                                            <option value="CE" <%= "CE".equals(estadoCliente) ? "selected" : ""%>>CE</option>
                                            <option value="DF" <%= "DF".equals(estadoCliente) ? "selected" : ""%>>DF</option>
                                            <option value="ES" <%= "ES".equals(estadoCliente) ? "selected" : ""%>>ES</option>
                                            <option value="GO" <%= "GO".equals(estadoCliente) ? "selected" : ""%>>GO</option>
                                            <option value="MA" <%= "MA".equals(estadoCliente) ? "selected" : ""%>>MA</option>
                                            <option value="MT" <%= "MT".equals(estadoCliente) ? "selected" : ""%>>MT</option>
                                            <option value="MS" <%= "MS".equals(estadoCliente) ? "selected" : ""%>>MS</option>
                                            <option value="MG" <%= "MG".equals(estadoCliente) ? "selected" : ""%>>MG</option>
                                            <option value="PA" <%= "PA".equals(estadoCliente) ? "selected" : ""%>>PA</option>
                                            <option value="PB" <%= "PB".equals(estadoCliente) ? "selected" : ""%>>PB</option>
                                            <option value="PR" <%= "PR".equals(estadoCliente) ? "selected" : ""%>>PR</option>
                                            <option value="PE" <%= "PE".equals(estadoCliente) ? "selected" : ""%>>PE</option>
                                            <option value="PI" <%= "PI".equals(estadoCliente) ? "selected" : ""%>>PI</option>
                                            <option value="RJ" <%= "RJ".equals(estadoCliente) ? "selected" : ""%>>RJ</option>
                                            <option value="RN" <%= "RN".equals(estadoCliente) ? "selected" : ""%>>RN</option>
                                            <option value="RS" <%= "RS".equals(estadoCliente) ? "selected" : ""%>>RS</option>
                                            <option value="RO" <%= "RO".equals(estadoCliente) ? "selected" : ""%>>RO</option>
                                            <option value="RR" <%= "RR".equals(estadoCliente) ? "selected" : ""%>>RR</option>
                                            <option value="SC" <%= "SC".equals(estadoCliente) ? "selected" : ""%>>SC</option>
                                            <option value="SP" <%= "SP".equals(estadoCliente) ? "selected" : ""%>>SP</option>
                                            <option value="SE" <%= "SE".equals(estadoCliente) ? "selected" : ""%>>SE</option>
                                            <option value="TO" <%= "TO".equals(estadoCliente) ? "selected" : ""%>>TO</option>
                                        </select>
                                    </div>
                                </div>

                                <h5 class="mb-3 mt-4">Renda e Ocupação</h5>
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label for="renda" class="form-label">Renda Mensal (R$)</label>
                                        <input type="number" step="0.01" class="form-control" id="renda" name="renda" value="<%= cliente.getRenda() != null ? cliente.getRenda() : ""%>">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Ocupação</label>
                                        <select class="form-select" name="ocupacao">
                                            <% String ocupacaoCliente = cliente.getOcupacao() != null ? cliente.getOcupacao() : "";%>
                                            <option <%= "Advogado".equals(ocupacaoCliente) ? "selected" : ""%>>Advogado</option>
                                            <option <%= "Administrador".equals(ocupacaoCliente) ? "selected" : ""%>>Administrador</option>
                                            <option <%= "Gerente".equals(ocupacaoCliente) ? "selected" : ""%>>Gerente</option>
                                            <option <%= "Estudante".equals(ocupacaoCliente) ? "selected" : ""%>>Estudante</option>
                                            <option <%= "Engenheiro".equals(ocupacaoCliente) ? "selected" : ""%>>Engenheiro</option>
                                            <option <%= "Cientista".equals(ocupacaoCliente) ? "selected" : ""%>>Cientista</option>
                                            <option <%= "Outro".equals(ocupacaoCliente) ? "selected" : ""%>>Outro</option>
                                        </select>
                                    </div>
                                </div>

                                <h5 class="mb-3 mt-4">Segurança</h5>
                                <div class="row g-3">
                                    <div class="col-md-4">
                                        <label for="senhaAtual" class="form-label">Senha Atual</label>
                                        <input type="password" class="form-control" id="senhaAtual" name="senhaAtual" placeholder="••••••••">
                                    </div>
                                    <div class="col-md-4">
                                        <label for="novaSenha" class="form-label">Nova Senha</label>
                                        <input type="password" class="form-control" id="novaSenha" name="novaSenha" placeholder="••••••••">
                                    </div>
                                    <div class="col-md-4">
                                        <label for="confirmarNovaSenha" class="form-label">Confirmar Nova Senha</label>
                                        <input type="password" class="form-control" id="confirmarNovaSenha" name="confirmarNovaSenha" placeholder="••••••••">
                                    </div>
                                </div>

                            </fieldset>

                            <div class="mt-4">
                                <button type="button" id="btnEditar" class="btn btn-primary">Editar Dados</button>
                                <button type="submit" id="btnSalvar" class="btn btn-success d-none">Salvar Alterações</button>
                                <button type="button" id="btnCancelar" class="btn btn-outline-secondary d-none">Cancelar</button>
                            </div>
                        </form>
                    </div>
                </div>
            </main>
        </div>

        <script>
            // mapeia os elementos do HTML para variáveis JavaScript
            const formDados = document.getElementById('formDados');
            const btnEditar = document.getElementById('btnEditar');
            const btnSalvar = document.getElementById('btnSalvar');
            const btnCancelar = document.getElementById('btnCancelar');

            // Adiciona um "ouvinte" para o evento de clique no botão Editar
            btnEditar.addEventListener('click', () => {
                // Remove o atributo 'disabled' do fieldset, tornando os campos editáveis
                formDados.disabled = false;
                // Esconde o botão 'Editar'
                btnEditar.classList.add('d-none');
                // Mostra os botões 'Salvar' e 'Cancelar'
                btnSalvar.classList.remove('d-none');
                btnCancelar.classList.remove('d-none');
            });

            // Adiciona um "ouvinte" para o evento de clique no botão Cancelar
            btnCancelar.addEventListener('click', () => {
                // Adiciona o atributo 'disabled' de volta, bloqueando os campos novamente
                formDados.disabled = true;
                // Mostra o botão 'Editar' de novo
                btnEditar.classList.remove('d-none');
                // Esconde os botões 'Salvar' e 'Cancelar'
                btnSalvar.classList.add('d-none');
                btnCancelar.classList.add('d-none');
                // recarrega a página descartando quaisquer alterações não salvas no formulário
                window.location.href = window.location.pathname;
            });
        </script>
    </body>
</html>