<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="br.uff.ic.grupo6.banco.model.Gerente" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
    // Medida de segurança: Verifica se o usuário logado é um Gerente
    if (!(session.getAttribute("usuarioLogado") instanceof Gerente)) {
        response.sendRedirect("../login.jsp?erro=Acesso Negado");
        return;
    }
    Gerente gerente = (Gerente) session.getAttribute("usuarioLogado");
%>
<!DOCTYPE html>
<html lang="pt-BR" data-bs-theme="dark">
    <head>
        <meta charset="UTF-8">
        <title>Dashboard do Gerente - Banco ATM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="../style.css" rel="stylesheet">
    </head>
    <body>
        <div class="d-flex">
            <nav class="sidebar bg-dark-subtle p-3">
                <h2 class="text-white mb-4"><i class="bi bi-coin text-warning"></i> Banco ATM</h2>
                <ul class="nav flex-column">
                    <li class="nav-item mb-2">
                        <a class="nav-link active" href="dashboard.jsp"><i class="bi bi-people-fill me-2"></i> Clientes</a>
                    </li>
                </ul>
                <div class="mt-auto">
                    <span class="d-block text-white-50 small mb-1">Logado como:</span>
                    <span class="d-block text-white fw-bold mb-3"><%= gerente.getNome() != null ? gerente.getNome() : "Gerente"%></span>
                    <a href="../LogoutServlet" class="btn btn-outline-secondary w-100">
                        <i class="bi bi-box-arrow-right me-2"></i> Sair
                    </a>
                </div>
            </nav>

            <main class="main-content">
                <div class="container-lg py-4 px-md-5">
                    <header class="mb-4 text-center"> 
                        <h3>Gerenciamento de Clientes</h3>
                        <p class="text-white-50">Digite o nome ou CPF do cliente para buscar ou liste todos os clientes.</p>
                    </header>

                    <div class="row justify-content-center">
                        <div class="col-lg-10 col-xl-9"> 
                            <form action="dashboard.jsp" method="get" class="mb-4">
                                <div class="input-group">
                                    <input type="text" class="form-control" name="termoBusca" placeholder="Digite o nome ou CPF..." value="${param.termoBusca}">
                                    <button class="btn btn-primary" type="submit">Buscar</button>
                                    <a href="dashboard.jsp?acao=listarTodos" class="btn btn-outline-secondary">Listar Todos</a>
                                </div>
                            </form>

                            <div class="card" style="background-color: #1e1e1e; border-color: #333;">
                                <div class="card-body p-0">
                                    <c:if test="${not empty param.termoBusca or param.acao == 'listarTodos'}">
                                        <jsp:include page="/gerente/BuscaClientesServlet" />
                                    </c:if>
                                </div>
                            </div>
                        </div> 
                    </div> 
                </div>
            </main>
        </div>
    </body>
</html>