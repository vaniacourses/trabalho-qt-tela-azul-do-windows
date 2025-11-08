<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="table-responsive">
    <table class="table table-dark table-hover mb-0 align-middle">
        <thead>
            <tr>
                <th scope="col">Nome</th>
                <th scope="col">CPF</th>
                <th scope="col">Email</th>
                <th scope="col" class="text-center">Ações</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="cliente" items="${listaClientes}">
                <tr>
                    <td><c:out value="${cliente.nome}" /></td>
                    <td><c:out value="${cliente.cpf}" /></td>
                    <td><c:out value="${cliente.email}" />

                    <td class="text-end">
                        <div class="d-grid gap-2 d-sm-flex justify-content-sm-end">
                            <a href="EditarClienteServlet?id=${cliente.id}" class="btn btn-success btn-sm">Editar</a>
                            <a href="ExcluirClienteServlet?id=${cliente.id}" class="btn btn-warning btn-sm" onclick="return confirm('Tem certeza que deseja excluir este cliente?')">Excluir</a>
                        </div>
                    </td>

                </tr>
            </c:forEach>
            <c:if test="${empty listaClientes}">
                <tr>
                    <td colspan="4" class="text-center py-4">Nenhum cliente cadastrado.</td>
                </tr>
            </c:if>
        </tbody>
    </table>
</div>