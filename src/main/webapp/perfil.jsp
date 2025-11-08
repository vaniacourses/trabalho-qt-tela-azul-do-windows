<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="br.uff.ic.grupo6.banco.model.Cliente"%>
<%@page import="br.uff.ic.grupo6.banco.model.Conta"%>
<!DOCTYPE html>
<html lang="pt-BR" data-bs-theme="dark">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Meu Perfil - Banco ATM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="style.css" rel="stylesheet">
    </head>

    <body>
        <%
            // Recupera o cliente da sessão
            Cliente cliente = null;
            if (session.getAttribute("usuarioLogado") instanceof Cliente) {
                cliente = (Cliente) session.getAttribute("usuarioLogado");
            } else {
                // Se não houver cliente na sessão, redireciona para o login.
                response.sendRedirect("login.jsp");
                return;
            }
            Conta conta = cliente.getConta();
        %>

        <div class="d-flex">
            <nav class="sidebar bg-dark-subtle p-3">
                <h2 class="text-white mb-4"><i class="bi bi-coin text-warning"></i> Banco ATM</h2>
                <ul class="nav flex-column">
                    <li class="nav-item mb-2">
                        <a class="nav-link" href="dashboard.jsp"><i class="bi bi-house-door-fill me-2"></i> Início</a>
                    </li>
                    <li class="nav-item mb-2">
                        <a class="nav-link active" href="#"><i class="bi bi-person-circle me-2"></i> Meu Perfil</a>
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
                        <h3>Meu Perfil</h3>
                        <a href="dashboard.jsp" class="btn btn-outline-secondary rounded-circle p-2 lh-1" title="Voltar para o Início">
                            <i class="bi bi-x-lg"></i>
                        </a>
                    </header>

                    <div class="row justify-content-center">
                        <div class="col-lg-8 col-xl-7">
                            <div class="card card-balance p-4">
                                <div class="row g-3 align-items-center">
                                    <div class="col-auto">
                                        <div class="bg-warning text-dark rounded-circle p-3 d-flex align-items-center justify-content-center" style="width: 60px; height: 60px;">
                                            <i class="bi bi-person-fill fs-2"></i>
                                        </div>
                                    </div>
                                    <div class="col">
                                        <h4 class="mb-0"><%= cliente.getNome()%></h4>
                                        <p class="text-white-50 mb-0">CPF: <%= cliente.getCpf()%></p>
                                    </div>
                                </div>

                                <hr class="my-4">

                                <h5 class="mb-3">Dados da Conta</h5>
                                <ul class="list-group list-group-flush">
                                    <li class="list-group-item bg-transparent d-flex justify-content-between">
                                        <span>Banco:</span>
                                        <strong>000 - Banco ATM</strong>
                                    </li>
                                    <li class="list-group-item bg-transparent d-flex justify-content-between">
                                        <span>Agência:</span>
                                        <strong><%= conta != null ? conta.getAgencia() : "N/D"%></strong>
                                    </li>
                                    <li class="list-group-item bg-transparent d-flex justify-content-between">
                                        <span>Conta Corrente:</span>
                                        <strong><%= conta != null ? conta.getNumero() : "N/D"%></strong>
                                    </li>
                                </ul>

                                <hr class="my-4">

                                <div class="text-center">
                                    <a href="DadosCadastraisServlet" class="btn btn-outline-secondary">
                                        <i class="bi bi-file-earmark-text me-2"></i>Meus Dados Cadastrais
                                    </a>
                                </div>

                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </body>
</html>