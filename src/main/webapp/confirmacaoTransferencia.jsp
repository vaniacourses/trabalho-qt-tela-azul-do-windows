<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="br.uff.ic.grupo6.banco.model.Cliente, br.uff.ic.grupo6.banco.model.Conta, java.text.NumberFormat" %>
<%
    Cliente clienteOrigem = (Cliente) request.getAttribute("clienteOrigem");
    Cliente clienteDestino = (Cliente) request.getAttribute("clienteDestino");
    Conta contaDestino = (Conta) request.getAttribute("contaDeDestino");
    Double valor = (Double) request.getAttribute("valor");

    if (clienteOrigem == null || clienteDestino == null || contaDestino == null || valor == null) {
        response.sendRedirect("transferencia.jsp?erro=Dados+inválidos+para+confirmação");
        return;
    }

    NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR"));
%>
<!DOCTYPE html>
<html lang="pt-BR" data-bs-theme="dark">
    <head>
        <title>Confirmar Transferência</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="style.css" rel="stylesheet">
    </head>
    <body>
        <div class="d-flex">
            <jsp:include page="/includes/sidebar.jsp" />
            <main class="main-content">
                <div class="container py-5">
                    <div class="row justify-content-center">
                        <div class="col-md-8 col-lg-7">
                            <div class="login-box p-4 p-md-5">
                                <div class="text-center mb-4">
                                    <i class="bi bi-shield-check text-info fs-1"></i>
                                    <h4 class="mt-2">Confirme os Dados da Transferência</h4>
                                    <p class="text-white-50">Por favor, verifique todos os dados antes de confirmar</p>
                                </div>

                                <h5>Origem</h5>
                                <p class="mb-0"><strong>Cliente:</strong> <%= clienteOrigem.getNome()%></p>
                                <p class="mb-0"><strong>CPF:</strong> <%= clienteOrigem.getCpf()%></p>
                                <p><strong>Agência:</strong> <%= clienteOrigem.getConta().getAgencia()%> / <strong>Conta:</strong> <%= clienteOrigem.getConta().getNumero()%></p>

                                <hr>

                                <h5>Destino</h5>
                                <p class="mb-0"><strong>Cliente:</strong> <%= clienteDestino.getNome()%></p>
                                <p class="mb-0"><strong>CPF:</strong> <%= clienteDestino.getCpf()%></p>
                                <p><strong>Agência:</strong> <%= contaDestino.getAgencia()%> / <strong>Conta:</strong> <%= contaDestino.getNumero()%></p>

                                <hr>

                                <div class="text-center fs-4">
                                    Valor: <strong class="text-warning"><%= formatoMoeda.format(valor)%></strong>
                                </div>

                                <form action="FinalizarTransferenciaServlet" method="post" class="mt-4">
                                    <input type="hidden" name="idContaOrigem" value="<%= clienteOrigem.getConta().getId()%>">
                                    <input type="hidden" name="idContaDestino" value="<%= contaDestino.getId()%>">
                                    <input type="hidden" name="valor" value="<%= valor%>">
                                    <input type="hidden" name="nomeDestino" value="<%= clienteDestino.getNome()%>">
                                    <input type="hidden" name="cpfDestino" value="<%= clienteDestino.getCpf()%>">
                                    <input type="hidden" name="agenciaDestino" value="<%= contaDestino.getAgencia()%>">
                                    <input type="hidden" name="numeroContaDestino" value="<%= contaDestino.getNumero()%>">

                                    <div class="d-flex justify-content-center gap-3">
                                        <button type="submit" class="btn btn-custom">Confirmar Transferência</button>
                                        <a href="dashboard.jsp" class="btn btn-outline-secondary">Cancelar</a>
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