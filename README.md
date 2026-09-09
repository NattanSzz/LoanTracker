# Controle de Empréstimos — Fase 1

Aplicativo Android pessoal, de usuário único, para substituir o controle de
empréstimos feito em fichas e caneta. Todos os dados ficam salvos localmente
no dispositivo (Room/SQLite); não há backend, servidor ou necessidade de
internet.

## Stack

Kotlin, Jetpack Compose, Material 3, Room, Navigation Compose, Coroutines.
Build via Gradle Wrapper — **não é necessário instalar o Android Studio**.

## Como compilar

### Pelo GitHub Actions (recomendado)

1. Crie um repositório no GitHub e envie este projeto (`git push`).
2. O workflow em `.github/workflows/android-build.yml` roda automaticamente
   a cada push na branch `main` (ou pode ser disparado manualmente na aba
   "Actions" → "Build APK" → "Run workflow").
3. Ao final, baixe o APK gerado na seção **Artifacts** da execução
   (`app-debug-apk`).

### Localmente, por linha de comando

Requer apenas JDK 17+ e o Android SDK command-line tools instalados (sem
precisar do Android Studio). Configure a variável `ANDROID_HOME` e um arquivo
`local.properties` com `sdk.dir=/caminho/para/o/sdk`, depois rode:

```bash
./gradlew assembleDebug
```

O APK fica em `app/build/outputs/apk/debug/app-debug.apk`.

## Decisões de cálculo (assumidas, pois o documento de escopo não detalhou a fórmula)

- A taxa de juros informada é aplicada **uma vez por parcela** (ex.: "10% ao
  mês" com 5 parcelas mensais = 5 períodos de incidência).
- **Juros simples:** `total = principal + (principal × taxa × parcelas)`.
- **Juros compostos:** `total = principal × (1 + taxa)^parcelas`.
- O valor de cada parcela é `total ÷ quantidade de parcelas`, com eventual
  diferença de arredondamento (poucos centavos) ajustada na última parcela,
  para que a soma das parcelas bata exatamente com o total a receber.
- Todos os valores monetários são armazenados como `Long` em **centavos**,
  nunca `Double`, conforme especificado.

Esses pontos podem ser ajustados facilmente em
`app/src/main/java/com/loantracker/app/domain/Calculations.kt` caso a forma
de cálculo desejada seja diferente.

## Estrutura do projeto

```
app/src/main/java/com/loantracker/app/
├── data/         # Entidades Room, DAOs, banco de dados, repositório
├── domain/       # Cálculo de juros, geração de parcelas, situação
└── ui/
    ├── home/          # Tela Início
    ├── cliente/       # Cadastrar cliente
    ├── emprestimo/    # Cadastrar empréstimo (com resumo de confirmação)
    ├── perfil/        # Perfil do cliente
    ├── detalhe/        # Detalhes do empréstimo + cronograma de parcelas
    ├── pagamento/     # Registrar pagamento (com suporte a pagamento parcial)
    ├── info/          # Tela Informações (resumo financeiro geral)
    ├── components/    # Composables reutilizáveis (cards, seletor de data, dropdown)
    └── navigation/    # Navigation Compose + barra inferior
```

## Escopo desta fase

Implementa tudo o que foi descrito no documento de especificação da Fase 1:
cadastro de clientes e empréstimos, cálculo automático de juros/parcelas/
vencimentos, registro de pagamentos (incluindo parciais), cronograma de
parcelas com situação (Pendente/Paga/Parcialmente paga/Vencida), perfil do
cliente com resumo financeiro, e tela de Informações com totais do ano,
parcelas por vencer/vencidas e lista de clientes em atraso.

Não foram adicionadas telas, campos ou funcionalidades além do que está no
documento de escopo.
