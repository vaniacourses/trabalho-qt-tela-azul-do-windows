<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="br.uff.ic.grupo6.banco.model.Cliente"%>
<%@page import="br.uff.ic.grupo6.banco.model.Conta"%>
<%@ page import="java.text.NumberFormat"%>
<!DOCTYPE html>
<html lang="pt-BR" data-bs-theme="dark">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Dashboard - Banco ATM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="style.css" rel="stylesheet">
    </head>
    <body>
        <%
            Cliente cliente = null;
            if (session.getAttribute("usuarioLogado") instanceof Cliente) {
                cliente = (Cliente) session.getAttribute("usuarioLogado");
            } else {
                response.sendRedirect("login.jsp");
                return;
            }
            Conta conta = cliente.getConta();
            NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR"));
            String saldoFormatado = formatoMoeda.format(conta != null ? conta.getSaldo() : 0.0);
        %>
        <div class="d-flex">
            <nav class="sidebar bg-dark-subtle p-3">
                <h2 class="text-white mb-4"><i class="bi bi-coin text-warning"></i> Banco ATM</h2>
                <%-- Menu lateral --%>
                <ul class="nav flex-column">
                    <li class="nav-item mb-2">
                        <a class="nav-link active" href="dashboard.jsp"><i class="bi bi-house-door-fill me-2"></i> Início</a>
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
                    <div class="row justify-content-center">
                        <div class="col-lg-11">
                            <header class="d-flex justify-content-between align-items-center mb-4">
                                <h3>Bem-vindo(a) de volta, <%= cliente.getNome()%>!</h3>
                                <a href="PerfilServlet" class="btn btn-outline-secondary rounded-circle p-2 lh-1" title="Meu Perfil">
                                    <i class="bi bi-person-fill fs-5"></i>
                                </a>
                            </header>
                            <%-- Mensagens de Sucesso e Erro --%>
                            <%
                                String msgSucesso = request.getParameter("msg");
                                String msgErro = request.getParameter("erro");
                                if (msgSucesso != null && !msgSucesso.isEmpty()) {
                            %>
                            <div style="max-width: 60%; margin-left: auto; margin-right: auto;">
                                <div class="alert alert-success alert-dismissible fade show" role="alert">
                                    <%= msgSucesso%>
                                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                                </div>
                            </div>
                            <%
                                }
                                if (msgErro != null && !msgErro.isEmpty()) {
                            %>
                            <div style="max-width: 60%; margin-left: auto; margin-right: auto;">
                                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                    <%= msgErro%>
                                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                                </div>
                            </div>
                            <%
                                }
                            %>
                            <div class="row">
                                <div class="col-md-7 col-lg-6">
                                    <div class="card card-balance p-3 mb-5">
                                        <div class="d-flex justify-content-between align-items-center">
                                            <div>
                                                <p class="card-text text-white-50 mb-1">Saldo em conta</p>
                                                <h2 id="saldoValor" class="card-title mb-0 h1"><%= saldoFormatado%></h2>
                                            </div>
                                            <i id="toggleSaldo" class="bi bi-eye fs-2 text-white-50" style="cursor: pointer;"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <h4 class="mb-3">O que deseja fazer hoje?</h4>
                            <div class="row g-3">
                                <div class="col-6 col-md-4 col-lg-4">
                                    <a href="saldo.jsp" class="card card-action text-center p-3 text-decoration-none h-100 d-flex flex-column justify-content-center">
                                        <i class="bi bi-wallet2 fs-1 mb-2"></i>
                                        <p class="mb-0 fw-semibold">Saldo</p>
                                    </a>
                                </div>
                                <div class="col-6 col-md-4 col-lg-4">
                                    <a href="extrato.jsp" class="card card-action text-center p-3 text-decoration-none h-100 d-flex flex-column justify-content-center">
                                        <i class="bi bi-receipt-cutoff fs-1 mb-2"></i>
                                        <p class="mb-0 fw-semibold">Extrato</p>
                                    </a>
                                </div>
                                <div class="col-6 col-md-4 col-lg-4">
                                    <a href="deposito.jsp" class="card card-action text-center p-3 text-decoration-none h-100 d-flex flex-column justify-content-center">
                                        <i class="bi bi-arrow-down-circle fs-1 mb-2"></i>
                                        <p class="mb-0 fw-semibold">Depósito</p>
                                    </a>
                                </div>
                                <div class="col-6 col-md-4 col-lg-4">
                                    <a href="saque.jsp" class="card card-action text-center p-3 text-decoration-none h-100 d-flex flex-column justify-content-center">
                                        <i class="bi bi-arrow-up-circle fs-1 mb-2"></i>
                                        <p class="mb-0 fw-semibold">Saque</p>
                                    </a>
                                </div>
                                <div class="col-6 col-md-4 col-lg-4">
                                    <a href="transferencia.jsp" class="card card-action text-center p-3 text-decoration-none h-100 d-flex flex-column justify-content-center">
                                        <i class="bi bi-send fs-1 mb-2"></i>
                                        <p class="mb-0 fw-semibold">Transferência</p>
                                    </a>
                                </div>
                                <div class="col-6 col-md-4 col-lg-4">
                                    <a href="investimento.jsp" class="card card-action text-center p-3 text-decoration-none h-100 d-flex flex-column justify-content-center">
                                        <i class="bi bi-graph-up-arrow fs-1 mb-2"></i>
                                        <p class="mb-0 fw-semibold">Investimento</p>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <script>
            document.getElementById('toggleSaldo').addEventListener('click', function (e) {
                const saldoEl = document.getElementById('saldoValor');
                const saldoOculto = 'R$ ••••••';
                const saldoReal = '<%= saldoFormatado%>';
                if (this.classList.contains('bi-eye')) {
                    saldoEl.textContent = saldoOculto;
                    this.classList.replace('bi-eye', 'bi-eye-slash');
                } else {
                    saldoEl.textContent = saldoReal;
                    this.classList.replace('bi-eye-slash', 'bi-eye');
                }
            });
            document.addEventListener('DOMContentLoaded', (event) => {
            });
        </script>
    </body>
</html>