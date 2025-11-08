<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="br.uff.ic.grupo6.banco.model.Cliente"%>
<%
    if (!(session.getAttribute("usuarioLogado") instanceof Cliente)) {
        response.sendRedirect("login.jsp");
        return;
    }
    String erro = request.getParameter("erro");
%>
<!DOCTYPE html>
<html lang="pt-BR" data-bs-theme="dark">
    <head>
        <meta charset="UTF-8">
        <title>Transferência - Banco ATM</title>
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
                        <div class="col-md-8 col-lg-7">
                            <div class="login-box p-4 p-md-5">
                                <div class="text-center mb-4">
                                    <i class="bi bi-send text-warning fs-2"></i>
                                    <h3 class="mt-1">Realizar Transferência</h3>
                                </div>
                                <form action="TransferenciaServlet" method="post">
                                    <% if (erro != null && !erro.isEmpty()) {%>
                                    <div class="alert alert-danger" role="alert">
                                        <%= erro%>
                                    </div>
                                    <% }%>
                                    <fieldset>
                                        <div class="row g-3">
                                            <div class="col-md-6">
                                                <div class="form-floating">
                                                    <input type="text" maxlength="4" class="form-control" id="agenciaDestino" name="agenciaDestino" placeholder="0000"  pattern="\d{4}" title="Digite apenas 4 dígitos numéricos">
                                                    <label for="agenciaDestino">Agência de Destino</label>
                                                </div>
                                            </div>
                                            <div class="col-md-6">
                                                <div class="form-floating">
                                                    <input type="text" maxlength="6" class="form-control" id="contaDestino" name="contaDestino" placeholder="000000" pattern="\d{6}" title="Digite apenas 6 dígitos numéricos">
                                                    <label for="contaDestino">Conta de Destino</label>
                                                </div>
                                            </div>
                                            <div class="col-12">
                                                <div class="form-floating">
                                                    <input type="number" maxlength="16" step="0.01" min="0.01" class="form-control" id="valor" name="valor" placeholder="Valor" >
                                                    <label for="valor">Valor a Transferir (R$)</label>
                                                </div>
                                            </div>
                                        </div>

                                        <div class="d-grid gap-2 mt-4" style="max-width: 300px; margin-left: auto; margin-right: auto;">
                                            <button type="submit" class="btn btn-custom btn-lg">Realizar Transferência</button>
                                            <a href="dashboard.jsp" class="btn btn-outline-secondary btn-lg">Cancelar</a>
                                        </div>

                                    </fieldset>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
        <script>

        </script>
    </body>
</html>