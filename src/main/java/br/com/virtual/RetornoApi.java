package br.com.virtual;

class RetornoApi {
    private Boolean erro; //true: COM ERRO; false: SEM ERRO;
    private Integer status;
    private String mensagemSucesso;
    private String mensagemErro;

    public RetornoApi(Boolean erro, Integer status, String mensagemSucesso, String mensagemErro) {
        this.erro = erro;
        this.status = status;
        this.mensagemSucesso = mensagemSucesso;
        this.mensagemErro = mensagemErro;
    }

    public Boolean getErro() {
        return erro;
    }

    public Integer getStatus() {
        return status;
    }

    public String getMensagemSucesso() {
        return mensagemSucesso;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }
}
