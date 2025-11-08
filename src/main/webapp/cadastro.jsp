<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="pt-BR">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Abra sua Conta - Banco ATM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="style.css" rel="stylesheet">
    </head>

    <body>
        <div class="container py-5">
            <div class="row justify-content-center">
                <div class="col-lg-9 col-xl-8">
                    <div class="login-box p-4 p-md-5">
                        <div class="text-center mb-4">
                            <h2 class="mb-4"><i class="bi bi-coin text-warning"></i> Banco ATM</h2>
                            <h3 class="mb-3"><i class="bi bi-person-plus-fill"></i> Crie sua Conta</h3>
                            <p class="text-white-50">Preencha seus dados para começar.</p>
                        </div>

                        <form action="CadastroServlet" method="post">

                            <%-- Exibir mensagens de erro que possam vir do servlet --%>
                            <% String msg = (String) request.getAttribute("mensagem");
                                if (msg != null) {%>
                            <div class="alert alert-danger" role="alert">
                                <%= msg%>
                            </div>
                            <% }%>

                            <h5 class="mb-3 mt-4">Dados Pessoais</h5>
                            <div class="row g-3">
                                <div class="col-12">
                                    <div class="form-floating">
                                        <input type="text" class="form-control" id="nome" name="nome" placeholder="Nome Completo" required>
                                        <label for="nome">Nome Completo</label>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="form-floating">
                                        <input type="text" class="form-control" id="cpf" name="cpf" placeholder="CPF" required>
                                        <label for="cpf">CPF</label>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="form-floating">
                                        <input type="date" class="form-control" id="dataNascimento" name="dataNascimento" placeholder="dd/mm/aaaa" required>
                                        <label for="dataNascimento">Data de Nascimento</label>
                                    </div>
                                </div>
                            </div>

                            <h5 class="mb-3 mt-4">Informações de Contato</h5>
                            <div class="row g-3">
                                <div class="col-md-7">
                                    <div class="form-floating">
                                        <input type="email" class="form-control" id="email" name="email" placeholder="E-mail" required>
                                        <label for="email">E-mail</label>
                                    </div>
                                </div>
                                <div class="col-md-5">
                                    <div class="form-floating">
                                        <input type="tel" class="form-control" id="telefone" name="telefone" placeholder="Telefone" required>
                                        <label for="telefone">Telefone</label>
                                    </div>
                                </div>
                            </div>

                            <h5 class="mb-3 mt-4">Endereço</h5>
                            <div class="row g-3">
                                <div class="col-md-4">
                                    <div class="form-floating">
                                        <input type="text" class="form-control" id="cep" name="cep" placeholder="CEP" required>
                                        <label for="cep">CEP</label>
                                    </div>
                                </div>
                                <div class="col-md-8">
                                    <div class="form-floating">
                                        <input type="text" class="form-control" id="endereco" name="endereco" placeholder="Endereço" required>
                                        <label for="endereco">Endereço (Rua, Nº)</label>
                                    </div>
                                </div>
                            </div>
                            <div class="row g-3 mt-1">
                                <div class="col-md-5">
                                    <div class="form-floating">
                                        <input type="text" class="form-control" id="bairro" name="bairro" placeholder="Bairro" required>
                                        <label for="bairro">Bairro</label>
                                    </div>
                                </div>
                                <div class="col-md-3">
                                    <div class="form-floating">
                                        <select class="form-select" id="estado" name="estado" required>
                                            <option value="" disabled selected>UF</option>
                                            <option value="AC">AC</option><option value="AL">AL</option><option value="AP">AP</option><option value="AM">AM</option><option value="BA">BA</option><option value="CE">CE</option><option value="DF">DF</option><option value="ES">ES</option><option value="GO">GO</option><option value="MA">MA</option><option value="MT">MT</option><option value="MS">MS</option><option value="MG">MG</option><option value="PA">PA</option><option value="PB">PB</option><option value="PR">PR</option><option value="PE">PE</option><option value="PI">PI</option><option value="RJ">RJ</option><option value="RN">RN</option><option value="RS">RS</option><option value="RO">RO</option><option value="RR">RR</option><option value="SC">SC</option><option value="SP">SP</option><option value="SE">SE</option><option value="TO">TO</option>
                                        </select>
                                        <label for="estado">Estado</label>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="form-floating">
                                        <input type="text" class="form-control" id="cidade" name="cidade" placeholder="Cidade" required>
                                        <label for="cidade">Cidade</label>
                                    </div>
                                </div>
                            </div>

                            <h5 class="mb-3 mt-4">Renda e Ocupação</h5>
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <div class="form-floating">
                                        <input type="number" step="0.01" class="form-control" id="renda" name="renda" placeholder="Renda Mensal" required>
                                        <label for="renda">Renda Mensal (R$)</label>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="form-floating">
                                        <select class="form-select" id="ocupacao" name="ocupacao" required>
                                            <option selected disabled value="">Selecione...</option>
                                            <option>Advogado</option>
                                            <option>Administrador</option>
                                            <option>Gerente</option>
                                            <option>Estudante</option>
                                            <option>Engenheiro</option>
                                            <option>Cientista</option>
                                            <option>Outro</option>
                                        </select>
                                        <label for="ocupacao">Ocupação</label>
                                    </div>
                                </div>
                            </div>

                            <h5 class="mb-3 mt-4">Segurança</h5>
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <div class="form-floating">
                                        <input type="password" class="form-control" id="senha" name="senha" placeholder="Crie sua senha" required>
                                        <label for="senha">Crie sua senha</label>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="form-floating">
                                        <input type="password" class="form-control" id="confirmaSenha" name="confirmaSenha" placeholder="Confirme sua senha" required>
                                        <label for="confirmaSenha">Confirme sua senha</label>
                                    </div>
                                </div>
                            </div>

                            <div class="d-flex flex-column align-items-center gap-2 mt-4">
                                <button type="submit" class="btn btn-custom w-50">Finalizar Cadastro</button>
                                <a href="login.jsp" class="btn btn-outline-secondary w-50">Voltar para o Login</a>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>