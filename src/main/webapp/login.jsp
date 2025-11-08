<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%-- prepara dados antes da página carregar --%>
<%
    // verifica se o filtro 'LembrarMeFiltro' enviou um CPF para ser pré-preenchido
    String cpfLembrado = (String) request.getAttribute("cpfLembrado");
    if (cpfLembrado == null) {
        // se não enviou, inicializa a variável como vazia para evitar erros
        cpfLembrado = "";
    }
    // prepara a string 'checked' que será usada no checkbox mais abaixo
    // Se o CPF foi lembrado, a caixa de seleção começará marcada
    String lembrarMarcado = !cpfLembrado.isEmpty() ? "checked" : "";
%>
<!DOCTYPE html>
<html lang="pt-BR" data-bs-theme="dark">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Bank - Login</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="style.css" rel="stylesheet">
    </head>

    <body>
        <div class="container d-flex justify-content-center align-items-center vh-100">
            <div class="col-lg-5 col-md-7 col-sm-9 col-11">
                <div class="login-box text-center">
                    <h1 class="mb-4"><i class="bi bi-coin text-warning"></i> Banco ATM</h1>

                    <form action="LoginServlet" method="post">
                        <div class="form-floating mb-3">
                            <input type="text" class="form-control" id="login" name="login" placeholder="CPF" value="<%= cpfLembrado%>" required>
                            <label for="login">CPF</label>
                        </div>
                        <div class="form-floating mb-3">
                            <input type="password" class="form-control" id="senha" name="senha" placeholder="Senha" required>
                            <label for="senha">Senha</label>
                        </div>

                        <%-- mensagens de sucesso ou erro vindas do servidor --%>
                        <%
                            request.setCharacterEncoding("UTF-8");
                            String msgSucesso = request.getParameter("msg");
                            if (msgSucesso != null) {
                        %>
                        <div class="alert alert-success mb-3" role="alert">
                            <%= msgSucesso%>
                        </div>
                        <%
                            }
                            // pega a mensagem de erro, de um 'forward' (atributo) ou de um 'redirect' (parâmetro)
                            String msgErro = (String) request.getAttribute("erro");
                            if (msgErro == null) {
                                msgErro = request.getParameter("erro");
                            }
                            if (msgErro != null) {
                        %>
                        <div class="alert alert-danger mb-3" role="alert">
                            <%= msgErro%>
                        </div>
                        <% }%>

                        <div class="form-check text-start mb-4">
                            <input class="form-check-input" type="checkbox" name="lembrar" id="lembrarCheck" <%= lembrarMarcado%>>
                            <label class="form-check-label text-white-50" for="lembrarCheck">
                                Lembrar meu login
                            </label>
                        </div>

                        <div class="d-flex flex-column align-items-center gap-2">
                            <button type="submit" class="btn btn-custom w-75">Entrar</button>
                            <a href="cadastro.jsp" class="btn btn-outline-secondary w-75">Abrir conta</a>
                        </div>

                    </form>

                    <div class="mt-4">
                        <a href="#" class="text-white-50">Esqueci minha senha</a>
                    </div>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>