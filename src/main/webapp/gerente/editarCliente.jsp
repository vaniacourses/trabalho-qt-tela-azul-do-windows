<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="br.uff.ic.grupo6.banco.model.Cliente"%>
<%@ page import="br.uff.ic.grupo6.banco.model.Gerente"%>
<%
    // Segurança: Apenas gerentes podem acessar
    if (!(session.getAttribute("usuarioLogado") instanceof Gerente)) {
        response.sendRedirect("../login.jsp?erro=Acesso Negado");
        return;
    }
    // Pega o cliente que o servlet colocou na requisição
    Cliente cliente = (Cliente) request.getAttribute("clienteParaEditar");
    if (cliente == null) {
        response.sendRedirect("dashboard.jsp?erro=Cliente não encontrado");
        return;
    }
%>
<!DOCTYPE html>
<html lang="pt-BR" data-bs-theme="dark">
    <head>
        <meta charset="UTF-8">
        <title>Editar Cliente - <%= cliente.getNome()%></title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="../style.css" rel="stylesheet">
    </head>
    <body>
        <div class="container mt-5 mb-5">
            <div class="row justify-content-center">
                <div class="col-md-8">
                    <div class="login-box p-4 p-md-5">
                        <h3>Editando Perfil de: <%= cliente.getNome()%></h3>
                        <p class="text-white-50">Altere os dados necessários e clique em salvar.</p>
                        <hr>

                        <form action="EditarClienteServlet" method="post">
                            <input type="hidden" name="id" value="<%= cliente.getId()%>" />

                            <h5 class="mb-3 mt-4">Dados Pessoais</h5>
                            <div class="row g-3">
                                <div class="col-12">
                                    <div class="form-floating">
                                        <input type="text" class="form-control" id="nome" name="nome" value="<%= cliente.getNome()%>" required>
                                        <label for="nome">Nome Completo</label>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="form-floating">
                                        <input type="text" class="form-control" id="cpf" name="cpf" value="<%= cliente.getCpf()%>" readonly>
                                        <label for="cpf">CPF (não pode ser alterado)</label>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="form-floating">
                                        <input type="date" class="form-control" id="dataNascimento" name="dataNascimento" value="<%= cliente.getDataNascimento()%>" required>
                                        <label for="dataNascimento">Data de Nascimento</label>
                                    </div>
                                </div>
                            </div>

                            <h5 class="mb-3 mt-4">Informações de Contato</h5>
                            <div class="row g-3">
                                <div class="col-md-7">
                                    <div class="form-floating">
                                        <input type="email" class="form-control" id="email" name="email" value="<%= cliente.getEmail() != null ? cliente.getEmail() : ""%>" required>
                                        <label for="email">E-mail</label>
                                    </div>
                                </div>
                                <div class="col-md-5">
                                    <div class="form-floating">
                                        <input type="tel" class="form-control" id="telefone" name="telefone" value="<%= cliente.getTelefone() != null ? cliente.getTelefone() : ""%>" required>
                                        <label for="telefone">Telefone</label>
                                    </div>
                                </div>
                            </div>

                            <h5 class="mb-3 mt-4">Endereço</h5>
                            <div class="row g-3">
                                <div class="col-md-4">
                                    <div class="form-floating">
                                        <input type="text" class="form-control" id="cep" name="cep" value="<%= cliente.getCep() != null ? cliente.getCep() : ""%>" required>
                                        <label for="cep">CEP</label>
                                    </div>
                                </div>
                                <div class="col-md-8">
                                    <div class="form-floating">
                                        <input type="text" class="form-control" id="endereco" name="endereco" value="<%= cliente.getEndereco() != null ? cliente.getEndereco() : ""%>" required>
                                        <label for="endereco">Endereço (Rua, Nº)</label>
                                    </div>
                                </div>
                            </div>
                            <div class="row g-3 mt-1">
                                <div class="col-md-5">
                                    <div class="form-floating">
                                        <input type="text" class="form-control" id="bairro" name="bairro" value="<%= cliente.getBairro() != null ? cliente.getBairro() : ""%>" required>
                                        <label for="bairro">Bairro</label>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="form-floating">
                                        <input type="text" class="form-control" id="cidade" name="cidade" value="<%= cliente.getCidade() != null ? cliente.getCidade() : ""%>" required>
                                        <label for="cidade">Cidade</label>
                                    </div>
                                </div>
                                <div class="col-md-3">
                                    <div class="form-floating">
                                        <input type="text" class="form-control" id="estado" name="estado" value="<%= cliente.getEstado() != null ? cliente.getEstado() : ""%>" required>
                                        <label for="estado">Estado (UF)</label>
                                    </div>
                                </div>
                            </div>

                            <h5 class="mb-3 mt-4">Renda e Ocupação</h5>
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <div class="form-floating">
                                        <input type="number" step="0.01" class="form-control" id="renda" name="renda" value="<%= cliente.getRenda() != null ? String.format(java.util.Locale.US, "%.2f", cliente.getRenda()) : ""%>" required>
                                        <label for="renda">Renda Mensal (R$)</label>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="form-floating">
                                        <input type="text" class="form-control" id="ocupacao" name="ocupacao" value="<%= cliente.getOcupacao() != null ? cliente.getOcupacao() : ""%>" required>
                                        <label for="ocupacao">Ocupação</label>
                                    </div>
                                </div>
                            </div>

                            <div class="d-grid gap-2 mt-4">
                                <button type="submit" class="btn btn-primary btn-lg">Salvar Alterações</button>
                                <a href="dashboard.jsp" class="btn btn-secondary btn-lg">Cancelar</a>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>