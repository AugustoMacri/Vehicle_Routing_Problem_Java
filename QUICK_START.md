# Guia Rápido - Sistema de Visualização de Rotas

## 🎯 Início Rápido

### 1️⃣ Executar uma Instância

```bash
./run_single_instance.sh 1  # C101
```

### 2️⃣ Gerar Mapas Visuais

```bash
./generate_route_maps.sh c101
```

### 3️⃣ Ver Resultados

**Dados Numéricos:**
- `resultsMulti/evo_c101.txt` - Fitness + Rotas detalhadas

**Mapas Visuais:**
- `resultsMulti/route_maps/C101/route_maps/route_map_c101_initial.png`
- `resultsMulti/route_maps/C101/route_maps/route_map_c101_final.png`

---

## 📊 Exemplos de Comandos

### Executar e Visualizar Única Instância

```bash
# C101
./run_single_instance.sh 1
./generate_route_maps.sh c101

# R101
./run_single_instance.sh 18
./generate_route_maps.sh r101

# RC101
./run_single_instance.sh 41
./generate_route_maps.sh rc101
```

### Gerar Mapas para Múltiplas Instâncias

```bash
# Executar todas as C1
for i in {1..9}; do
    ./run_single_instance.sh $i
done

# Gerar todos os mapas C1
./generate_route_maps.sh all_c1
```

### Validação Completa (10 Execuções)

```bash
# Executar validação
python3 scripts/run_validation_c1.py

# Gerar mapas para resultados de validação
for instance in C101 C102 C103 C104 C105 C106 C107 C108 C109; do
    python3 scripts/plot_route_maps.py \
        --instance $instance \
        --results-dir results_validation_C1_previous/$instance \
        --output-dir results_validation_C1_previous/$instance/route_maps
done
```

---

## 📁 Estrutura de Arquivos

### Executáveis
- `run_single_instance.sh` - Executa uma instância
- `run_all_instances.sh` - Executa todas as 26 instâncias
- `generate_route_maps.sh` - Gera mapas de rotas

### Scripts Python
- `scripts/plot_route_maps.py` - Gerador de mapas principal
- `scripts/run_validation_c1.py` - Validação com 10 execuções

### Resultados
```
resultsMulti/
├── evo_*.txt          # Resultados de execução única
├── stats/             # Estatísticas
└── route_maps/        # Mapas visuais
    └── C101/
        └── route_maps/
            ├── *_initial.png
            └── *_final.png

results_validation_C1_previous/
├── C101/
│   ├── evo_c101_exec01.txt
│   ├── ...
│   └── route_maps/
└── ...
```

---

## 🎨 Interpretando os Mapas

### Elementos Visuais
- 🔴 **Quadrado Vermelho**: Depósito
- ⚫ **Círculos Pretos**: Clientes
- 🔢 **Números**: IDs dos clientes
- 🌈 **Linhas Coloridas**: Rotas (uma cor por veículo)
- ➡️ **Setas**: Direção do percurso

### Análise de Qualidade

**Mapa Inicial (before evolution):**
- Mais cruzamentos
- Rotas menos organizadas
- Pode ter mais veículos

**Mapa Final (after 3000 generations):**
- Menos cruzamentos
- Rotas mais compactas
- Melhor agrupamento geográfico
- Possivelmente menos veículos

### Indicadores Positivos
✅ Rotas formam "pétalas" ao redor do depósito  
✅ Poucos cruzamentos entre rotas  
✅ Distribuição equilibrada de clientes  
✅ Rotas compactas geograficamente

---

## 🔍 Análise dos Resultados

### Arquivo `evo_*.txt`

**1. Tabela de Fitness**
```
Subpopulação\Geração    g0      g100    ...     g3000
subPopPonderation       6756.49 5423.14 ...     4228.42
```
- Valores devem **diminuir** ao longo das gerações
- Melhor fitness = menor valor

**2. Seções de Rotas**
```
Veículo X: Depósito(0) -> Cliente(Y) -> ... -> Depósito(0)
    Clientes: N | Demanda: XX/200 | Distância: YY.YY
```
- Compare distância total inicial vs final
- Verifique se capacidade (200) é respeitada
- Analise número de veículos utilizados

---

## ⚙️ Parâmetros do Algoritmo

### Configurações Atuais
- **População**: 900 indivíduos
  - 70% com K-means clustering (630)
  - 30% com Gillet-Miller (270)
- **Gerações**: 3000
- **Taxa de Elitismo**: Definida em App.java
- **Multi-objetivo**: 
  - Distância × 1.0
  - Tempo × 0.5
  - Combustível × 0.75

---

## 🐛 Troubleshooting

### Erro: "Arquivo evo_*.txt não encontrado"
**Solução:** Execute primeiro `./run_single_instance.sh <numero>`

### Erro: ModuleNotFoundError: matplotlib
**Solução:** 
```bash
pip install matplotlib numpy
```

### Mapas não são gerados
**Verificar:**
1. Arquivo `src/instances/solomon/C101.txt` existe?
2. Arquivo `resultsMulti/evo_c101.txt` foi criado?
3. Python3 está instalado?

### Permissão negada ao executar scripts
**Solução:**
```bash
chmod +x run_single_instance.sh
chmod +x generate_route_maps.sh
```

---

## 📚 Documentação Completa

Para informações detalhadas, consulte:
- `ROUTE_VISUALIZATION_README.md` - Sistema de visualização
- `SCRIPTS_README.md` - Scripts de execução
- `IMPLEMENTATION_SUMMARY.md` - Detalhes técnicos da implementação

---

## 🚀 Workflow Recomendado

### Para Testar uma Instância

```bash
# 1. Executar
./run_single_instance.sh 1

# 2. Visualizar
./generate_route_maps.sh c101

# 3. Analisar
cat resultsMulti/evo_c101.txt | tail -50
```

### Para Validação Completa

```bash
# 1. Validar (10 execuções cada)
python3 scripts/run_validation_c1.py

# 2. Gerar mapas
./generate_route_maps.sh all_c1

# 3. Analisar estatísticas
ls results_validation_C1_previous/C*/resultados_aemmt_*.txt
```

---

## 📊 Benchmark Solomon

### Instâncias C (Clustered)
- Clientes agrupados geograficamente
- Janelas de tempo estreitas
- **Esperado**: Rotas com clusters bem definidos

### Instâncias R (Random)
- Clientes distribuídos aleatoriamente
- Janelas de tempo amplas
- **Esperado**: Rotas mais dispersas

### Instâncias RC (Random-Cluster)
- Mistura de características C e R
- **Esperado**: Comportamento intermediário

---

## ✅ Checklist de Verificação

Após executar e gerar mapas:

- [ ] Arquivo `evo_*.txt` contém tabela de fitness
- [ ] Arquivo `evo_*.txt` contém seções de rotas (inicial e final)
- [ ] Fitness ponderado diminuiu ao longo das gerações
- [ ] Mapas PNG foram gerados (inicial e final)
- [ ] Mapa final mostra menos cruzamentos que o inicial
- [ ] Distância total no final < distância inicial
- [ ] Capacidade dos veículos respeitada (≤200)
- [ ] Todos os clientes são atendidos

---

## 📞 Suporte

Em caso de problemas:
1. Verifique os logs de execução
2. Consulte a documentação detalhada
3. Verifique dependências (Python, Java, matplotlib)
4. Revise permissões dos scripts
