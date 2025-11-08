<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="br.uff.ic.grupo6.banco.model.Cliente"%>
<%
    // Segurança
    if (!(session.getAttribute("usuarioLogado") instanceof Cliente)) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="pt-BR" data-bs-theme="dark">
    <head>
        <meta charset="UTF-8">
        <title>Investimentos - Banco ATM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="style.css" rel="stylesheet">
    </head>
    <body>
        <div class="d-flex">

            <%-- Menu lateral --%>
            <jsp:include page="/includes/sidebar.jsp" />

            <main class="main-content">
                <div class="container-lg py-4 px-md-5">
                    <div class="row justify-content-center">
                        <div class="col-lg-10">

                            <div class="text-center mb-4">
                                <i class="bi bi-graph-up-arrow text-warning" style="font-size: 2rem;"></i>
                                <h3 class="mt-2">Opções de Investimento</h3>
                            </div>

                            <div class="row g-4">
                                <div class="col-md-6">
                                    <a href="aplicarInvestimento.jsp?tipoInvestimento=SELIC" class="card card-action text-decoration-none h-100">
                                        <div class="card-body">
                                            <h5 class="card-title">Tesouro Selic</h5>
                                            <p class="card-text text-white-50">Investimento de baixo risco atrelado à taxa básica de juros.</p>
                                        </div>
                                    </a>
                                </div>
                                <div class="col-md-6">
                                    <a href="aplicarInvestimento.jsp?tipoInvestimento=CDB" class="card card-action text-decoration-none h-100">
                                        <div class="card-body">
                                            <h5 class="card-title">CDB 110% do CDI</h5>
                                            <p class="card-text text-white-50">Renda fixa com rentabilidade superior à poupança.</p>
                                        </div>
                                    </a>
                                </div>
                                <div class="col-md-6">
                                    <a href="aplicarInvestimento.jsp?tipoInvestimento=FII" class="card card-action text-decoration-none h-100">
                                        <div class="card-body">
                                            <h5 class="card-title">Fundos Imobiliários (FIIs)</h5>
                                            <p class="card-text text-white-50">Invista no mercado imobiliário e receba aluguéis mensais.</p>
                                        </div>
                                    </a>
                                </div>
                                <div class="col-md-6">
                                    <a href="aplicarInvestimento.jsp?tipoInvestimento=POUPANCA" class="card card-action text-decoration-none h-100">
                                        <div class="card-body">
                                            <h5 class="card-title">Poupança</h5>
                                            <p class="card-text text-white-50">Invista na caderneta de poupança.</p>
                                        </div>
                                    </a>
                                </div>
                                <div class="col-md-6 mx-auto">
                                    <a href="meusInvestimentos.jsp" class="card card-action text-decoration-none h-100">
                                        <div class="card-body">
                                            <h5 class="card-title">Meus Investimentos</h5>
                                            <p class="card-text text-white-50">Visualize todos os seus investimentos.</p>
                                        </div>
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