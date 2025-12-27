# 📑 ÍNDICE GERAL - Documentação do Projeto VRP

## 🎯 Navegação Rápida

### Para Começar
- **[QUICK_START.md](QUICK_START.md)** ⭐ - Comece aqui! Guia rápido de uso
- **[README.md](README.md)** - Visão geral do projeto
- **[EXECUTIVE_SUMMARY.md](EXECUTIVE_SUMMARY.md)** - Sumário executivo da implementação

### Scripts e Automação
- **[SCRIPTS_README.md](SCRIPTS_README.md)** - Documentação dos scripts bash
- **Executáveis:**
  - `run_single_instance.sh` - Executa uma instância
  - `run_all_instances.sh` - Executa todas as 26 instâncias
  - `generate_route_maps.sh` - Gera mapas de rotas

### Sistema de Visualização
- **[ROUTE_VISUALIZATION_README.md](ROUTE_VISUALIZATION_README.md)** - Sistema completo de visualização
- **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** - Detalhes técnicos
- **[COMPLETION_REPORT.md](COMPLETION_REPORT.md)** - Relatório detalhado da implementação

### Scripts Python
- **`scripts/plot_route_maps.py`** - Geração de mapas visuais
- **`scripts/run_validation_c1.py`** - Framework de validação
- **`scripts/compare_results.py`** - Comparação de resultados
- **`scripts/visualize_results_*.py`** - Visualização de fitness

---

## 📚 Organização por Tipo de Usuário

### 👨‍💻 Desenvolvedor - Primeiro Uso
1. Leia [README.md](README.md) - Entenda o projeto
2. Leia [QUICK_START.md](QUICK_START.md) - Configure o ambiente
3. Execute `./run_single_instance.sh 1` - Teste básico
4. Execute `./generate_route_maps.sh c101` - Gere visualização
5. Explore [ROUTE_VISUALIZATION_README.md](ROUTE_VISUALIZATION_README.md)

### 🔬 Pesquisador - Análise de Resultados
1. Leia [SCRIPTS_README.md](SCRIPTS_README.md) - Entenda os scripts
2. Execute `python3 scripts/run_validation_c1.py` - Validação sistemática
3. Use `./generate_route_maps.sh all_c1` - Visualizações completas
4. Consulte [ROUTE_VISUALIZATION_README.md](ROUTE_VISUALIZATION_README.md) - Interpretação

### 🎓 Acadêmico - Publicação/Apresentação
1. Leia [EXECUTIVE_SUMMARY.md](EXECUTIVE_SUMMARY.md) - Visão geral
2. Execute validações com `run_validation_c1.py`
3. Gere mapas profissionais com `generate_route_maps.sh`
4. Consulte [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) - Detalhes técnicos

### 🛠️ Mantenedor - Desenvolvimento/Debug
1. Leia [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) - Arquitetura
2. Consulte [COMPLETION_REPORT.md](COMPLETION_REPORT.md) - Histórico
3. Revise código em `src/main/App.java` - Implementação
4. Analise `src/genetic/` - Algoritmos genéticos

---

## 📂 Estrutura de Diretórios

```
Vehicle_Routing_Problem_Java/
│
├── 📄 Documentação Principal
│   ├── README.md                           # Visão geral do projeto
│   ├── QUICK_START.md                      # ⭐ Guia rápido
│   ├── EXECUTIVE_SUMMARY.md                # Sumário executivo
│   └── INDEX.md                            # Este arquivo
│
├── 📖 Documentação Técnica
│   ├── ROUTE_VISUALIZATION_README.md       # Sistema de visualização
│   ├── SCRIPTS_README.md                   # Scripts de execução
│   ├── IMPLEMENTATION_SUMMARY.md           # Detalhes de implementação
│   └── COMPLETION_REPORT.md                # Relatório completo
│
├── 🔧 Scripts de Execução
│   ├── run_single_instance.sh              # Executa uma instância
│   ├── run_all_instances.sh                # Executa todas (26)
│   ├── run_multiple.sh                     # Executa múltiplas
│   └── generate_route_maps.sh              # Gera mapas visuais
│
├── 🐍 Scripts Python
│   └── scripts/
│       ├── plot_route_maps.py              # Geração de mapas
│       ├── run_validation_c1.py            # Validação C1
│       ├── compare_results.py              # Comparação
│       ├── visualize_results_mono.py       # Visualização mono
│       └── visualize_results_multi.py      # Visualização multi
│
├── ☕ Código Fonte Java
│   └── src/
│       ├── main/
│       │   └── App.java                    # Entrada principal
│       ├── genetic/                        # Algoritmos genéticos
│       │   ├── Population.java
│       │   ├── Individual.java
│       │   ├── KMeansClusteringInitializer.java
│       │   ├── Crossover.java
│       │   ├── Mutation.java
│       │   └── *FitnessCalculator.java
│       ├── vrp/                            # Estruturas VRP
│       │   ├── BenchMarkReader.java
│       │   ├── Client.java
│       │   └── ProblemInstance.java
│       └── instances/
│           └── solomon/                    # Instâncias benchmark
│
└── 📊 Resultados
    ├── resultsMulti/                       # Execuções individuais
    │   ├── evo_*.txt                       # Fitness + Rotas
    │   ├── stats/                          # Estatísticas
    │   └── route_maps/                     # Mapas visuais
    │       └── */route_maps/*.png
    │
    └── results_validation_C1_previous/     # Validação sistemática
        └── C10*/
            ├── evo_*_exec*.txt
            └── route_maps/*.png
```

---

## 🎯 Guias por Tarefa

### Executar uma Instância
```bash
./run_single_instance.sh 1        # C101
./generate_route_maps.sh c101
```
📖 Documentação: [QUICK_START.md](QUICK_START.md#-início-rápido)

### Validação Completa (10×)
```bash
python3 scripts/run_validation_c1.py
./generate_route_maps.sh all_c1
```
📖 Documentação: [QUICK_START.md](QUICK_START.md#validação-completa-10-execuções)

### Análise de Resultados
```bash
cat resultsMulti/evo_c101.txt
ls resultsMulti/route_maps/
```
📖 Documentação: [ROUTE_VISUALIZATION_README.md](ROUTE_VISUALIZATION_README.md#interpretação-dos-mapas)

### Comparar Múltiplas Execuções
```bash
python3 scripts/compare_results.py
```
📖 Documentação: [SCRIPTS_README.md](SCRIPTS_README.md)

### Modificar Parâmetros
Edite `src/main/App.java`:
- `pop_size` - Tamanho da população
- `numGenerations` - Número de gerações
- `elitismRate` - Taxa de elitismo
- Pesos multi-objetivo

📖 Documentação: [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md#parâmetros-do-algoritmo)

---

## 🔍 Busca Rápida

### Como fazer...

**...executar C101?**
```bash
./run_single_instance.sh 1
```

**...gerar mapas de rotas?**
```bash
./generate_route_maps.sh c101
```

**...validar 10 vezes cada instância C1?**
```bash
python3 scripts/run_validation_c1.py
```

**...visualizar evolução do fitness?**
```bash
python3 scripts/visualize_results_multi.py
```

**...comparar múltiplas execuções?**
```bash
python3 scripts/compare_results.py
```

**...modificar parâmetros do algoritmo?**
Edite `src/main/App.java`

**...entender o formato dos arquivos?**
Veja [ROUTE_VISUALIZATION_README.md](ROUTE_VISUALIZATION_README.md#formato-do-arquivo-evo_txt)

**...interpretar os mapas?**
Veja [QUICK_START.md](QUICK_START.md#-interpretando-os-mapas)

---

## 🆘 Troubleshooting

### Erro de Compilação
📖 [QUICK_START.md](QUICK_START.md#erro-modulenotfounderror-matplotlib)

### Arquivo não encontrado
📖 [QUICK_START.md](QUICK_START.md#erro-arquivo-evo_txt-não-encontrado)

### Mapas não gerados
📖 [QUICK_START.md](QUICK_START.md#mapas-não-são-gerados)

### Permissão negada
📖 [QUICK_START.md](QUICK_START.md#permissão-negada-ao-executar-scripts)

### Outros problemas
📖 [QUICK_START.md](QUICK_START.md#-troubleshooting)

---

## 🌟 Funcionalidades Destacadas

### ✨ K-means Clustering
Inicialização inteligente que reduz distância inicial em 30-40%
📖 [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md#k-means-clustering)

### 🎨 Visualização de Rotas
Mapas coloridos em alta resolução mostrando rotas de cada veículo
📖 [ROUTE_VISUALIZATION_README.md](ROUTE_VISUALIZATION_README.md)

### 🤖 Automação Completa
Scripts para execução e análise de múltiplas instâncias
📖 [SCRIPTS_README.md](SCRIPTS_README.md)

### 📊 Multi-Objetivo
Otimização simultânea de distância, tempo e combustível
📖 [README.md](README.md#-funcionalidades-principais)

---

## 📞 Referências Cruzadas

### Documentos por Tópico

**Inicialização:**
- K-means: [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)
- Gillet-Miller: [README.md](README.md)

**Visualização:**
- Sistema: [ROUTE_VISUALIZATION_README.md](ROUTE_VISUALIZATION_README.md)
- Scripts: [SCRIPTS_README.md](SCRIPTS_README.md)
- Uso: [QUICK_START.md](QUICK_START.md)

**Validação:**
- Framework: [SCRIPTS_README.md](SCRIPTS_README.md)
- Exemplos: [QUICK_START.md](QUICK_START.md)

**Implementação:**
- Detalhes: [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)
- Relatório: [COMPLETION_REPORT.md](COMPLETION_REPORT.md)
- Sumário: [EXECUTIVE_SUMMARY.md](EXECUTIVE_SUMMARY.md)

---

## 📈 Progresso do Projeto

### ✅ Implementado
- [x] Algoritmo genético multi-objetivo
- [x] K-means clustering para inicialização
- [x] 4 calculadores de fitness
- [x] Sistema de visualização de rotas
- [x] Scripts de automação
- [x] Framework de validação
- [x] Documentação completa

### 🔄 Em Desenvolvimento
- [ ] Integração automática de mapas no validation script
- [ ] Dashboard web interativo
- [ ] Animações de evolução

### 💡 Planejado
- [ ] Comparação visual multi-execução
- [ ] Estatísticas visuais avançadas
- [ ] Interface gráfica completa

---

## 📝 Histórico de Versões

### v1.0 (26/12/2024)
- ✅ Sistema de visualização de rotas implementado
- ✅ Scripts de automação criados
- ✅ Documentação completa

### v0.9 (Anterior)
- ✅ K-means clustering implementado
- ✅ Algoritmo genético multi-objetivo funcional

---

## 🎓 Para Estudantes

### Conceitos Implementados
- Algoritmos Genéticos
- Otimização Multi-Objetivo
- K-means Clustering
- Vehicle Routing Problem (VRP)
- Benchmark Solomon

### Estruturas de Dados
- Arrays multidimensionais (rotas)
- Lists e Streams (população)
- HashMaps (coordenadas)

### Padrões de Projeto
- Strategy (calculadores de fitness)
- Factory (inicializadores)
- Template Method (algoritmo genético)

---

## 🏆 Benchmarks

### Instâncias Solomon
- **C (Clustered):** C101-C109
- **R (Random):** R101-R109
- **RC (Random-Cluster):** RC101-RC108

Total: **26 instâncias** suportadas

---

## 📧 Informações Adicionais

### Repositório
- Estrutura bem organizada
- Documentação extensiva
- Exemplos práticos

### Licença
- MIT (conforme projeto)

### Contribuições
- Fork e pull requests bem-vindos
- Issues para bugs e sugestões

---

**Última Atualização:** 26 de Dezembro de 2024  
**Versão do Índice:** 1.0  
**Mantido por:** Augusto

---

## 🔗 Links Rápidos

| Documento | Descrição | Link |
|-----------|-----------|------|
| Quick Start | ⭐ Comece aqui | [QUICK_START.md](QUICK_START.md) |
| README | Visão geral | [README.md](README.md) |
| Visualização | Sistema de mapas | [ROUTE_VISUALIZATION_README.md](ROUTE_VISUALIZATION_README.md) |
| Scripts | Automação | [SCRIPTS_README.md](SCRIPTS_README.md) |
| Implementação | Detalhes técnicos | [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) |
| Relatório | Completo | [COMPLETION_REPORT.md](COMPLETION_REPORT.md) |
| Sumário | Executivo | [EXECUTIVE_SUMMARY.md](EXECUTIVE_SUMMARY.md) |

---

**🎯 Dica:** Para começar rapidamente, leia [QUICK_START.md](QUICK_START.md) e execute os comandos básicos!
