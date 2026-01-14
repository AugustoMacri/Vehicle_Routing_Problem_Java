#!/usr/bin/env python3
"""
Análise da velocidade e cálculo de tempo nas instâncias Solomon
"""

import math

print("=" * 80)
print("ANÁLISE DE VELOCIDADE E CÁLCULO DE TEMPO - Instâncias Solomon")
print("=" * 80)
print()

# Dados de uma instância Solomon (C101) como exemplo
print("Exemplo da instância C101:")
print("-" * 80)
print("Cliente 0 (Depósito): X=40, Y=50, Ready=0, Due=1236")
print("Cliente 1:            X=45, Y=68, Ready=912, Due=967, Service=90")
print()

# Coordenadas
depot = (40, 50)
client1 = (45, 68)

# Distância Euclidiana
distance = math.sqrt((client1[0] - depot[0])**2 + (client1[1] - depot[1])**2)
print(f"Distância Euclidiana (depósito -> cliente 1): {distance:.2f} unidades")
print()

# Na literatura Solomon:
print("INFORMAÇÕES DA LITERATURA SOLOMON:")
print("-" * 80)
print("1. Coordenadas: Unidades arbitrárias (não especificadas)")
print("2. Tempo: Em unidades de tempo (geralmente minutos)")
print("3. Velocidade padrão na literatura: NÃO É ESPECIFICADA!")
print()
print("IMPORTANTE:")
print("  - Solomon (1987) NÃO especifica velocidade")
print("  - A distância Euclidiana É o tempo de viagem diretamente")
print("  - Ou seja: tempo_viagem = distância (sem conversão de velocidade)")
print()

# Comparação com o código atual
print("=" * 80)
print("CÓDIGO ATUAL:")
print("=" * 80)
print(f"VEHICLE_SPEED = 50 km/h")
print(f"Cálculo: tempo = (distância / velocidade) * 60")
print()
print(f"Para distância = {distance:.2f}:")
print(
    f"  tempo_calculado = ({distance:.2f} / 50) * 60 = {(distance/50)*60:.2f} minutos")
print()

print("=" * 80)
print("CÓDIGO CORRETO (Literatura Solomon):")
print("=" * 80)
print("VELOCITY = 1 (ou não usar velocidade)")
print("Cálculo: tempo = distância")
print()
print(f"Para distância = {distance:.2f}:")
print(f"  tempo_correto = {distance:.2f} unidades de tempo")
print()

# Análise do impacto
print("=" * 80)
print("IMPACTO DO ERRO:")
print("=" * 80)
print(f"Tempo atual:   {(distance/50)*60:.2f} minutos")
print(f"Tempo correto: {distance:.2f} minutos")
print(f"Fator de erro: {((distance/50)*60) / distance:.2f}x")
print()
print("⚠️  O tempo está sendo calculado com um fator de 1.2x (20% a mais)")
print("    Isso pode explicar as violações de janelas de tempo!")
print()

print("=" * 80)
print("RECOMENDAÇÃO:")
print("=" * 80)
print("1. Mudar VEHICLE_SPEED de 50 para 1")
print("2. OU remover a divisão por velocidade (tempo = distância)")
print("3. Reexecutar todas as validações")
print()

# Verificar com janelas de tempo reais
print("=" * 80)
print("VERIFICAÇÃO COM JANELAS DE TEMPO REAIS:")
print("=" * 80)
print(f"Cliente 1: Ready={912}, Due={967}")
print(f"Tempo de viagem do depósito: {distance:.2f} minutos")
print(f"Chegada se sair do depósito em t=0: t={distance:.2f}")
print(f"✓ Está dentro da janela [912, 967]? NÃO! Muito cedo.")
print()
print("Mas se a rota for otimizada, o veículo sai mais tarde.")
print("O importante é que o tempo seja proporcional à distância.")
print()

print("=" * 80)
print("CONCLUSÃO:")
print("=" * 80)
print("O código está INCORRETO segundo a literatura Solomon!")
print("A velocidade deveria ser 1, não 50.")
print("Isso está causando:")
print("  - Tempos de viagem inflados em 20%")
print("  - Possíveis violações de janelas de tempo artificiais")
print("  - Resultados não comparáveis com benchmarks")
