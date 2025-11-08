<%-- Menu lateral reutilizável --%>
<%@ page pageEncoding="UTF-8" %>
<nav class="sidebar bg-dark-subtle p-3">
    <h2 class="text-white mb-4"><i class="bi bi-coin text-warning"></i> Banco ATM</h2>

    <ul class="nav flex-column">
        <li class="nav-item mb-2">
            <a class="nav-link" href="${pageContext.request.contextPath}/dashboard.jsp"><i class="bi bi-house-door-fill me-2"></i> Início</a>
        </li>
        <li class="nav-item mb-2">
            <a class="nav-link" href="${pageContext.request.contextPath}/saldo.jsp"><i class="bi bi-wallet2 me-2"></i> Saldo</a>
        </li>
        <li class="nav-item mb-2">
            <a class="nav-link" href="${pageContext.request.contextPath}/extrato.jsp"><i class="bi bi-receipt-cutoff me-2"></i> Extrato</a>
        </li>
        <li class="nav-item mb-2">
            <a class="nav-link" href="${pageContext.request.contextPath}/deposito.jsp"><i class="bi bi-arrow-down-circle me-2"></i> Depósito</a>
        </li>
        <li class="nav-item mb-2">
            <a class="nav-link" href="${pageContext.request.contextPath}/saque.jsp"><i class="bi bi-arrow-up-circle me-2"></i> Saque</a>
        </li>
        <li class="nav-item mb-2">
            <a class="nav-link" href="${pageContext.request.contextPath}/transferencia.jsp"><i class="bi bi-send me-2"></i> Transferência</a>
        </li>
        <li class="nav-item mb-2">
            <a class="nav-link" href="${pageContext.request.contextPath}/investimento.jsp"><i class="bi bi-graph-up-arrow me-2"></i> Investimento</a>
        </li>
    </ul>

    <div class="mt-auto">
        <a href="${pageContext.request.contextPath}/LogoutServlet" class="btn btn-outline-secondary w-100"><i class="bi bi-box-arrow-right me-2"></i> Sair</a>
    </div>
</nav>