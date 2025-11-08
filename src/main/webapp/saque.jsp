<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="br.uff.ic.grupo6.banco.model.Cliente"%>
<%
    if (!(session.getAttribute("usuarioLogado") instanceof Cliente)) {
        response.sendRedirect("login.jsp");
        return;
    }
    // String erro = request.getParameter("erro"); // Já está declarado no bloco abaixo
%>
<!DOCTYPE html>
<html lang="pt-BR" data-bs-theme="dark">
    <head>
        <meta charset="UTF-8">
        <title>Saque - Banco ATM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="style.css" rel="stylesheet">
    </head>
    <body>
        <div class="d-flex">

            <%-- Menu lateral --%>
            <jsp:include page="/includes/sidebar.jsp" />

            <main class="main-content d-flex align-items-center">
                <div class="container">
                    <div class="row justify-content-center">
                        <div class="col-md-6 col-lg-5">
                            <div class="login-box p-4 p-md-5">

                                <div class="d-flex flex-column align-items-center mb-4">
                                    <div class="d-flex align-items-center">
                                        <i class="bi bi-cash-coin text-success fs-1 me-2"></i>
                                        <i class="bi bi-arrow-up-circle text-warning fs-1"></i>
                                    </div>
                                    <h3 class="mt-2">Realizar Saque</h3>
                                </div>

                                <% String erro = request.getParameter("erro"); %>
                                <% if (erro != null && !erro.isEmpty()) {%>
                                <div class="alert alert-danger" role="alert">
                                    <%= erro%>
                                </div>
                                <% }%>

                                <form action="SaqueServlet" method="post">
                                    <div class="form-floating mb-3">
                                        <input type="number" step="0.01" min="0.01" class="form-control" id="valor" name="valor" placeholder="Valor do saque" required>
                                        <label for="valor">Valor do Saque (R$)</label>
                                    </div>
                                    <div class="d-grid gap-2 mt-4">
                                        <button type="submit" class="btn btn-custom btn-lg">Confirmar Saque</button>
                                        <a href="dashboard.jsp" class="btn btn-outline-secondary btn-lg">Cancelar</a>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </body>
</html>