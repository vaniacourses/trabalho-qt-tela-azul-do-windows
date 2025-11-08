<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="br.uff.ic.grupo6.banco.model.Cliente"%>
<%@ page import="br.uff.ic.grupo6.banco.model.Conta"%>
<%@ page import="br.uff.ic.grupo6.banco.model.Transacao"%>
<%@ page import="java.time.format.DateTimeFormatter"%>
<%@ page import="java.text.NumberFormat"%>
<%
    Cliente clienteOrigem = (Cliente) session.getAttribute("usuarioLogado");
    Transacao comprovante = (Transacao) request.getAttribute("comprovante");
    String nomeDestino = (String) request.getAttribute("nomeDestino");
    String cpfDestino = (String) request.getAttribute("cpfDestino");
    String agenciaDestino = (String) request.getAttribute("agenciaDestino");
    String numeroContaDestino = (String) request.getAttribute("numeroContaDestino");

    if (clienteOrigem == null || comprovante == null) {
        response.sendRedirect("login.jsp?erro=Sessão+expirada");
        return;
    }

    NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR"));

    DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss");
%>
<!DOCTYPE html>
<html lang="pt-BR" data-bs-theme="dark">
    <head>
        <meta charset="UTF-8">
        <title>Comprovante de Transferência - Banco ATM</title>
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
                                    <h4 class="mt-2">Transferência Realizada com Sucesso</h4>
                                    <p class="text-white-50">Confira os detalhes do seu comprovante</p>
                                </div>

                                <div class="comprovante-section">
                                    <h5>Dados da Transação</h5>
                                    <ul class="list-group list-group-flush bg-transparent mb-3">
                                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent text-white">
                                            <span>ID da Transação:</span> <span><%= comprovante.getId()%></span>
                                        </li>
                                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent text-white">
                                            <span>Data e Hora:</span> <span><%= comprovante.getDataTransacao().format(formatoData)%></span>
                                        </li>
                                    </ul>

                                    <h5>Conta de Origem</h5>
                                    <ul class="list-group list-group-flush bg-transparent mb-3">
                                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent text-white">
                                            <span>Cliente:</span> <strong><%= clienteOrigem.getNome()%></strong>
                                        </li>
                                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent text-white">
                                            <span>CPF:</span> <strong><%= clienteOrigem.getCpf()%></strong>
                                        </li>
                                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent text-white">
                                            <span>Agência:</span> <strong><%= clienteOrigem.getConta().getAgencia()%></strong>
                                        </li>
                                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent text-white">
                                            <span>Conta:</span> <strong><%= clienteOrigem.getConta().getNumero()%></strong>
                                        </li>
                                    </ul>

                                    <h5>Conta de Destino</h5>
                                    <ul class="list-group list-group-flush bg-transparent mb-3">
                                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent text-white">
                                            <span>Cliente:</span> <strong><%= nomeDestino%></strong>
                                        </li>
                                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent text-white">
                                            <span>CPF:</span> <strong><%= cpfDestino%></strong>
                                        </li>
                                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent text-white">
                                            <span>Agência:</span> <strong><%= agenciaDestino%></strong>
                                        </li>
                                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent text-white">
                                            <span>Conta:</span> <strong><%= numeroContaDestino%></strong>
                                        </li>
                                    </ul>

                                    <hr>
                                    <div class="text-center fs-5 fw-bold d-flex justify-content-between align-items-center">
                                        <span>Valor Transferido:</span>
                                        <span class="text-success fs-4"><%= formatoMoeda.format(comprovante.getValor())%></span>
                                    </div>
                                    <hr>
                                </div>

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