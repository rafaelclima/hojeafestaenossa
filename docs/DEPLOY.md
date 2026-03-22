# Deploy no Dokploy

Tutorial completo para deploy da aplicação backend Spring Boot no Dokploy.

---

## Pré-requisitos

- Servidor com Dokploy instalado
- Repositório GitHub conectado ao Dokploy
- PostgreSQL criado (pode ser pelo Dokploy ou externo)
- Bucket OCI criado na Oracle Cloud
- Chave API OCI gerada e baixada

---

## 1. Conectar GitHub ao Dokploy

1. Acesse o painel Dokploy
2. Vá em **Settings** → **GitHub Provider**
3. Clique em **Connect** e autorize o acesso ao repositório

---

## 2. Criar o Banco de Dados (PostgreSQL)

### Opção A: PostgreSQL gerenciado pelo Dokploy

1. No painel Dokploy, vá em **Project** → **Add Database**
2. Selecione **PostgreSQL**
3. Configure:
   - **Name**: `hojeafestaenossa-db`
   - **Database Name**: `hojeafestaenossa`
   - **Database User**: `postgres`
   - **Database Password**: `(gere uma senha forte)`
   - **Docker Image**: `postgres:16`
4. Clique em **Create**

### Opção B: PostgreSQL existente

Se já possui um PostgreSQL externo, anote:
- **Internal Host**: `hostname-do-banco`
- **Port**: `5432`
- **Database Name**: `hojeafestaenossa`
- **Username**: `postgres`
- **Password**: `sua-senha`

---

## 3. Criar a Aplicação no Dokploy

1. No painel Dokploy, vá em **Add Application**
2. Selecione **Java** ou **Dockerfile**
3. Configure:
   - **Name**: `hojeafestaenossa-backend`
   - **Git Repository**: selecione seu repositório GitHub
   - **Branch**: `main`
   - **Build Type**: **Dockerfile**
   - **Dockerfile Path**: `Dockerfile`
   - **Docker Context**: `.`
   - **Port**: `8080`
4. Clique em **Save**

---

## 4. Configurar Environment Variables

Na aba **Environment Variables** da aplicação, adicione:

| Variável | Valor |
|----------|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<host>:5432/hojeafestaenossa` |
| `SPRING_DATASOURCE_USERNAME` | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | `<senha-do-banco>` |
| `OCI_TENANCY_ID` | `<valor do console OCI>` |
| `OCI_USER_ID` | `<valor do console OCI>` |
| `OCI_FINGERPRINT` | `<valor do console OCI>` |
| `OCI_PRIVATE_KEY_PATH` | `/secrets/oci/private.key` |
| `OCI_NAMESPACE` | `<namespace do Object Storage>` |
| `OCI_REGION` | `sa-saopaulo-1` |
| `APP_BASE_URL` | `https://seudominio.com` |

### Obtendo valores OCI

1. Acesse [Console Oracle Cloud](https://cloud.oracle.com/)
2. **Identity & Security** → **Users** → selecione seu usuário
3. **API Keys** - copie os valores de:
   - **Tenant ID**: mesmo do tenancy
   - **User ID**: ID do usuário
   - **Fingerprint**: da chave criada
4. **Object Storage** → **Namespace Details** - copie o namespace

---

## 5. Configurar a Chave OCI (File Mount)

1. Na aplicação Dokploy, vá em **Volumes**
2. Clique em **Add Volume**
3. Selecione **File Mount**
4. Configure:
   - **Content**: Abra o arquivo `.pem` da chave privada (ex: `~/.oci/oci_api_key.pem`) e copie todo o conteúdo, incluindo `-----BEGIN PRIVATE KEY-----` e `-----END PRIVATE KEY-----`
   - **File Path**: `private.key`
   - **Mount Path**: `/secrets/oci/private.key`
5. Clique em **Save**

---

## 6. Deploy Inicial

1. Clique no botão **Deploy** no painel da aplicação
2. Aguarde o build e startup
3. Verifique os logs em **Logs** → **Container**

---

## 7. Verificar Saúde

```bash
# Health check
curl http://localhost:8080/actuator/health
```

Ou no painel Dokploy, vá em **Advanced** → **Health Check**.

---

## 8. Configurar Domínio (Opcional)

1. Na aplicação, vá em **Domains**
2. Clique em **Add Domain**
3. Configure:
   - **Domain Name**: `api.seudominio.com`
   - **HTTPS**: `true` (Dokploy gerencia Let's Encrypt automaticamente)
4. Save

---

## Variáveis de Ambiente Completas

```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/hojeafestaenossa
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=<senha>
OCI_TENANCY_ID=<tenancy-id>
OCI_USER_ID=<user-id>
OCI_FINGERPRINT=<fingerprint>
OCI_PRIVATE_KEY_PATH=/secrets/oci/private.key
OCI_NAMESPACE=<namespace>
OCI_REGION=sa-saopaulo-1
APP_BASE_URL=https://seudominio.com
```

---

## Troubleshooting

### Aplicação não inicia

1. Vá em **Logs** → verifique os erros
2. Common issues:
   - **Database connection failed**: verifique `SPRING_DATASOURCE_URL` e credenciais
   - **OCI key not found**: verifique o File Mount está correto

### Erro OCI

```bash
# Verificar se chave foi montada
docker exec -it <container> ls -la /secrets/oci/
```

### Health check falhando

Verifique se o Spring Actuator está respondendo:
```bash
curl http://localhost:8080/actuator/health
```

---

## Atualizar Aplicação

1. Faça push para o branch `main` (ou o branch configurado)
2. O Dokploy detecta automaticamente (se auto-deploy estiver habilitado)
3. Ou clique em **Redeploy** no painel

---

## Auto-Deploy com GitHub

1. Na aplicação, vá em **General**
2. Ative **Auto Deploy**
3. Configure um webhook no GitHub (opcional - o Dokploy già detecta push)

---

**Última atualização:** 21 de março de 2026