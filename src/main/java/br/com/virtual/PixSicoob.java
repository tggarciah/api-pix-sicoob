package br.com.virtual;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

public class PixSicoob {
    private Integer ambiente;
    private String urlToken;
    private String urlBasePix;
    private String certificadoCaminho;
    private String certificadoSenha;
    private String clientId;
    private String grantType;
    private String scope;
    private String expiresIn;
    private CloseableHttpClient httpClient;
    private Token token;

    public void criarConexaoHttp() throws UnrecoverableKeyException, CertificateException, KeyStoreException, IOException,
            NoSuchAlgorithmException, KeyManagementException {
        httpClient = criarHttpClientComContextoSSL();
        token = gerarToken();
    }

    public String criarCobrancaComVencimento(String txId, String requestBody) {
        if (txId.isEmpty() || txId.isBlank())
            return getRetornoApiErro(0, "O id da transação (txid) é obrigatório.");

        try {
            String url = getUrlBasePix() + "/cobv/" + txId;
            HttpPut httpPut = new HttpPut(url);

            System.out.println(getClientId());


            httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader("Authorization", "Bearer " + token.getAccessToken());
            httpPut.setHeader("client_id", getClientId());

            httpPut.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

            HttpResponse response = httpClient.execute(httpPut);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                //@autor: Thiago Gonçalves Garcia; @data: 2023-09-14
                //@comentario: O statusCode 201 é o único código de sucesso documentado na API no momento do desenvolvimento.
                if (statusCode == 201) {
                    return getRetornoApiSucesso(response.getStatusLine().getStatusCode(), EntityUtils.toString(entity));
                } else {
                    return getRetornoApiErro(response.getStatusLine().getStatusCode(), EntityUtils.toString(entity));
                }
            } else {
                return gerarErroSemRespostaDoServidor();
            }
        } catch (IOException e) {
            return gerarErroExecucaoHttpClient(e);
        }
    }

    public String revisarCobrancaComVencimento(String txId, String requestBody) {
        if (txId.isEmpty() || txId.isBlank())
            return getRetornoApiErro(0, "O id da transação (txid) é obrigatório.");

        try {
            String url = getUrlBasePix() + "/cobv/" + txId;
            HttpPatch httpPatch = new HttpPatch(url);

            httpPatch.setHeader("Accept", "application/json");
            httpPatch.setHeader("Authorization", "Bearer " + token.getAccessToken());
            httpPatch.setHeader("client_id", getClientId());

            httpPatch.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

            HttpResponse response = this.httpClient.execute(httpPatch);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                //@autor: Thiago Gonçalves Garcia; @data: 2023-09-14
                //@comentario: O statusCode 200 é o único código de sucesso documentado na API no momento do desenvolvimento.
                if (statusCode == 200) {
                    return getRetornoApiSucesso(response.getStatusLine().getStatusCode(), EntityUtils.toString(entity));
                } else {
                    return getRetornoApiErro(response.getStatusLine().getStatusCode(), EntityUtils.toString(entity));
                }
            } else {
                return gerarErroSemRespostaDoServidor();
            }
        } catch (IOException e) {
            return gerarErroExecucaoHttpClient(e);
        }
    }

    public String consultarCobrancaComVencimento(String txId, Integer revisao) {
        if (txId.isEmpty() || txId.isBlank())
            return getRetornoApiErro(0, "O id da transação (txid) é obrigatório.");

        StringBuilder url = new StringBuilder();
        url.append(getUrlBasePix()).append("/cobv/").append(txId);
        if (revisao > 0)
            url.append("?revisao=").append(revisao);

        System.out.println(url.toString());
        try {
            HttpGet httpGet = new HttpGet(url.toString());

            System.out.println(token.getAccessToken());

            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Authorization", "Bearer " + token.getAccessToken());
            httpGet.setHeader("client_id", getClientId());

            HttpResponse response = this.httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                //@autor: Thiago Gonçalves Garcia; @data: 2023-09-14
                //@comentario: O statusCode 200 é o único código de sucesso documentado na API no momento do desenvolvimento.
                if (statusCode == 200) {
                    return getRetornoApiSucesso(response.getStatusLine().getStatusCode(), EntityUtils.toString(entity));
                } else {
                    return getRetornoApiErro(response.getStatusLine().getStatusCode(), EntityUtils.toString(entity));
                }
            } else {
                return gerarErroSemRespostaDoServidor();
            }
        } catch (IOException e) {
            return gerarErroExecucaoHttpClient(e);
        }
    }

    public String consultarCobrancaListaComVencimento(String dataInicio, String dataFim, String documento,
                                                      String status, Integer loteCobVId, Integer paginaAtual,
                                                      Integer itensPorPagina) {
        if (dataInicio.isEmpty() || dataInicio.isBlank())
            return getRetornoApiErro(HttpStatus.SC_BAD_REQUEST, "A data início é obrigatória.");
        if (dataFim.isEmpty() || dataFim.isBlank())
            return getRetornoApiErro(HttpStatus.SC_BAD_REQUEST, "A data fim é obrigatória.");
        if (!(documento.isBlank() || documento.isEmpty()) && !(documento.length() == 11 || documento.length() == 14))
            return getRetornoApiErro(HttpStatus.SC_BAD_REQUEST, "O documento CPF ou CNPJ está com o tamanho inválido.");

        StringBuilder params = new StringBuilder(String.format("inicio=%1$s&fim=%2$s", dataInicio, dataFim));

        if (documento.length() == 11) {
            params.append(String.format("&cpf=%s", documento));
        } else if (documento.length() == 14) {
            params.append(String.format("&cnpj=%s", documento));
        }
        if (!(status.isEmpty() || status.isBlank()))
            params.append(String.format("&status='%s'", status));
        if (loteCobVId > 0)
            params.append(String.format("&loteCobVId=%s", loteCobVId));
        if (paginaAtual > 0)
            params.append(String.format("&paginacao.paginaAtual=%s", paginaAtual));
        if (itensPorPagina > 0)
            params.append(String.format("&paginacao.itensPorPagina=%s", itensPorPagina));

        try {
            String url = getUrlBasePix() + "/cobv?" + params;

            HttpGet httpGet = new HttpGet(url);

            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Authorization", "Bearer " + token.getAccessToken());
            httpGet.setHeader("client_id", getClientId());

            HttpResponse response = this.httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                //@autor: Thiago Gonçalves Garcia; @data: 2023-09-14
                //@comentario: O statusCode 200 é o único código de sucesso documentado na API no momento do desenvolvimento.
                if (statusCode == 200) {
                    return getRetornoApiSucesso(response.getStatusLine().getStatusCode(), EntityUtils.toString(entity));
                } else {
                    return getRetornoApiErro(response.getStatusLine().getStatusCode(), EntityUtils.toString(entity));
                }
            } else {
                return gerarErroSemRespostaDoServidor();
            }
        } catch (IOException e) {
            return gerarErroExecucaoHttpClient(e);
        }
    }

    private Token gerarToken() throws IOException {
        HttpPost httpPost = new HttpPost(getUrlToken());
        // Atribui cabeçalho
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        // Configura os parâmetros da requisição POST
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", getGrantType()));
        params.add(new BasicNameValuePair("client_id", getClientId()));
        params.add(new BasicNameValuePair("scope", getScope()));
        params.add(new BasicNameValuePair("expires_in", getExpiresIn()));

        httpPost.setEntity(new UrlEncodedFormEntity(params));

        // Executa a requisição
        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();

        ObjectMapper mapper = new ObjectMapper();

        if (entity != null) {
            return mapper.readValue(EntityUtils.toString(entity), Token.class);
        } else {
            return null;
        }
    }

    private CloseableHttpClient criarHttpClientComContextoSSL() throws UnrecoverableKeyException, CertificateException,
            KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = criarContextoSSLComCertificado();
        return HttpClients
                .custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
    }

    private SSLContext criarContextoSSLComCertificado() throws CertificateException, KeyStoreException, IOException,
            NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        KeyStore keyStore = carregarCertificadoPfx();
        return SSLContextBuilder.create().loadKeyMaterial(keyStore, getCertificadoSenha().toCharArray()).build();
    }

    private KeyStore carregarCertificadoPfx() throws KeyStoreException, IOException, CertificateException,
            NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(getCertificadoCaminho()), getCertificadoSenha().toCharArray());
        return keyStore;
    }

    private String getRetornoApiSucesso(Integer status, String mensagem) {
        RetornoApi retornoApi = new RetornoApi(false, status, mensagem, null);
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(retornoApi);
        } catch (JsonProcessingException e) {
            return erroProcessamentoObjetoParaJson(e);
        }
    }

    private String getRetornoApiErro(Integer status, String mensagem) {
        RetornoApi retornoApi = new RetornoApi(true, status, null, mensagem);
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(retornoApi);
        } catch (JsonProcessingException e) {
            return erroProcessamentoObjetoParaJson(e);
        }
    }

    private String erroProcessamentoObjetoParaJson(JsonProcessingException e) {
        String auxMsg = "Erro ao converter o objeto de retorno da API em JSON: " + e.getMessage();
        return "{'erro':true,'status':0,'mensagemErro':'" + auxMsg + "'}";
    }

    private String gerarErroSemRespostaDoServidor() {
        String mensagem = "Erro: não foi possível obter resposta do servidor.";
        return getRetornoApiErro(null, mensagem);
    }

    private String gerarErroExecucaoHttpClient(IOException e) {
        String mensagem = "Erro: não foi possível abrir conexão com o servidor - " + e.getMessage();
        return getRetornoApiErro(null, mensagem);
    }

    private Integer getAmbiente() {
        return ambiente;
    }

    public void setAmbiente(Integer ambiente) {
        this.ambiente = ambiente;
    }

    private String getUrlToken() {
        if (urlToken == null && getAmbiente() == 1)
            return getRetornoApiErro(0, "Erro: A URL para obter o token deve ser informada.");
        return urlToken;
    }

    public void setUrlToken(String urlToken) {
        this.urlToken = urlToken;
    }

    private String getUrlBasePix() {
        if (urlBasePix == null)
            return getRetornoApiErro(0, "Erro: A URL base da API Pix token deve ser informada.");
        return urlBasePix;
    }

    public void setUrlBasePix(String urlBasePix) {
        this.urlBasePix = urlBasePix;
    }

    private String getClientId() {
        if (this.clientId == null)
            return getRetornoApiErro(0, "Erro: A identificação do cliente (client_id) deve ser informada.");
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    private String getCertificadoCaminho() {
        if (certificadoCaminho == null && getAmbiente() == 1)
            return getRetornoApiErro(0, "Erro: O caminho absoluto do certificado digital PFX deve ser informado.");
        return certificadoCaminho;
    }

    public void setCertificadoCaminho(String certificadoCaminho) {
        this.certificadoCaminho = certificadoCaminho;
    }

    private String getCertificadoSenha() {
        if (certificadoSenha == null)
            return getRetornoApiErro(0, "Erro: O senha do certificado digital PFX deve ser informada.");
        return certificadoSenha;
    }

    public void setCertificadoSenha(String certificadoSenha) {
        this.certificadoSenha = certificadoSenha;
    }

    private String getGrantType() {
        if (grantType == null)
            return getRetornoApiErro(0, "Erro: O valor \"tipo de concessão\" (grant_type) é obrigatório.");
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    private String getScope() {
        if (scope == null)
            return getRetornoApiErro(0, "Erro: O valor \"Escopo de acesso\" (scope) é obrigatório.");
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    private String getExpiresIn() {
        if (expiresIn == null)
            return getRetornoApiErro(0, "Erro: O valor \"Expirar em\" (expires_in) é obrigatório.");
        return expiresIn;
    }

    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }
}