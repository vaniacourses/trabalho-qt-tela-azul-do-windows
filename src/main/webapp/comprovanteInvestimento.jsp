<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="br.uff.ic.grupo6.banco.model.Cliente"%>
<%@ page import="br.uff.ic.grupo6.banco.model.Conta"%>
<%@ page import="br.uff.ic.grupo6.banco.model.Investimento"%>
<%@ page import="java.time.format.DateTimeFormatter"%>
<%@ page import="java.text.NumberFormat"%>
<%@ page import="java.util.Locale"%>

<%
    Cliente cliente = (Cliente) session.getAttribute("usuarioLogado");
    Investimento comprovante = (Investimento) request.getAttribute("comprovante");

    if (cliente == null || comprovante == null) {
        response.sendRedirect("login.jsp?erro=Sessão expirada ou dados inválidos para o comprovante.");
        return;
    }

    Conta conta = cliente.getConta();
    NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss");
%>
<!DOCTYPE html>
<html lang="pt-BR" data-bs-theme="dark">
    <head>
        <meta charset="UTF-8">
        <title>Comprovante de Investimento - Banco ATM</title>
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
                                    <i class="bi bi-check-circle-fill text-success fs-1"></i>
                                    <h4 class="mt-2">Investimento Realizado com Sucesso</h4>
                                    <p class="text-white-50">Confira os detalhes do seu comprovante</p>
                                </div>

                                <ul class="list-group list-group-flush bg-transparent">
                                    <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent text-white">
                                        <span>ID do Investimento:</span> <span><%= comprovante.getId()%></span>
                                    </li>
                                    <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent text-white">
                                        <span>Tipo de Investimento:</span> <span class="text-uppercase"><%= comprovante.getTipoInvestimento()%></span>
                                    </li>
                                    <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent text-white">
                                        <span>Data e Hora:</span> <span><%= comprovante.getDataAplicacao().format(formatoData)%></span>
                                    </li>
                                    <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent text-white">
                                        <span>Cliente:</span> <strong><%= cliente.getNome()%></strong>
                                    </li>
                                    <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent text-white">
                                        <span>CPF:</span> <strong><%= cliente.getCpf()%></strong>
                                    </li>
                                    <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent text-white">
                                        <span>Agência:</span> <strong><%= conta != null ? conta.getAgencia() : "N/D"%></strong>
                                    </li>
                                    <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent text-white">
                                        <span>Conta:</span> <strong><%= conta != null ? conta.getNumero() : "N/D"%></strong>
                                    </li>
                                    <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent text-white fs-5 fw-bold">
                                        <span>Valor Aplicado:</span>
                                        <span class="text-success"><%= formatoMoeda.format(comprovante.getValorAplicado())%></span>
                                    </li>
                                </ul>

                                <div class="text-center mt-4">
                                    <a href="dashboard.jsp" class="btn btn-outline-secondary">Voltar ao Início</a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </body>
</html>