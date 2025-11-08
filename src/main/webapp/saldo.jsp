<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="br.uff.ic.grupo6.banco.model.Cliente"%>
<%@ page import="br.uff.ic.grupo6.banco.model.Conta"%>
<%@ page import="java.text.NumberFormat"%>
<%@ page import="java.util.Locale"%>
<%
    // Segurança e recuperação de dados
    if (!(session.getAttribute("usuarioLogado") instanceof Cliente)) {
        response.sendRedirect("login.jsp");
        return;
    }
    Cliente cliente = (Cliente) session.getAttribute("usuarioLogado");
    Conta conta = cliente.getConta();

    // Formatação de moeda
    NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    String saldoFormatado = formatoMoeda.format(conta != null ? conta.getSaldo() : 0.0);
%>
<!DOCTYPE html>
<html lang="pt-BR" data-bs-theme="dark">
    <head>
        <meta charset="UTF-8">
        <title>Consulta de Saldo - Banco ATM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="style.css" rel="stylesheet">
    </head>
    <body>
        <div class="d-flex">

            <%-- Menu lateral --%>
            <jsp:include page="/includes/sidebar.jsp" />

            <main class="main-content d-flex align-items-center">
                <div class="container-lg">

                    <div class="row justify-content-center">
                        <div class="col-lg-8 col-xl-7">
                            <div class="card card-balance p-4 mb-4">

                                <div class="text-center mb-4">
                                    <i class="bi bi-wallet2 text-warning" style="font-size: 2rem;"></i>
                                    <h3 class="mt-2">Consulta de Saldo</h3>
                                </div>

                                <ul class="list-group list-group-flush">
                                    <li class="list-group-item bg-transparent d-flex justify-content-between text-white"><span>Agência:</span> <strong><%= conta != null ? conta.getAgencia() : "N/D"%></strong></li>
                                    <li class="list-group-item bg-transparent d-flex justify-content-between text-white"><span>Conta Corrente:</span> <strong><%= conta != null ? conta.getNumero() : "N/D"%></strong></li>
                                    <li class="list-group-item bg-transparent d-flex justify-content-between text-white align-items-center fs-4 mt-2">
                                        <span>Saldo Atual:</span> 
                                        <strong class="text-success"><%= saldoFormatado%></strong>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </body>
</html>