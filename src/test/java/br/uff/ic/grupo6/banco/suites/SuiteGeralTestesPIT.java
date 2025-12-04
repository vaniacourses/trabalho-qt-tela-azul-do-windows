package br.uff.ic.grupo6.banco.suites;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

// Suite para rodar PIT Test em múltiplos pacotes do projeto
@Suite
@SelectPackages({
    "br.uff.ic.grupo6.banco.estrutural", 
    "br.uff.ic.grupo6.banco.mutacao"
})
public class SuiteGeralTestesPIT {
    // "conteiner" para rodar os pacotes acima juntos
}