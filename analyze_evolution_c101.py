#!/usr/bin/env python3
"""
Análise detalhada dos resultados C101
"""

import re


def analyze_evolution(filename):
    """Analisa a evolução do fitness e violações"""
    with open(filename, 'r') as f:
        lines = f.readlines()

    # Encontrar linha subPopDistance
    for line in lines:
        if line.startswith('subPopDistance'):
            values = line.strip().split('\t')[1:]
            generations = [int(re.search(r'g(\d+)', h).group(1))
                           for h in lines[0].strip().split('\t')[1:]]

            print("=" * 80)
            print("EVOLUÇÃO DO FITNESS - Subpopulação Distance (C101)")
            print("=" * 80)
            print()

            # Parse valores
            fitness_values = []
            for v in values:
                if v.strip():
                    fitness_values.append(float(v.replace(',', '.')))

            # Mostrar evolução
            print(
                f"{'Geração':<10} {'Fitness':<15} {'Est. Violações':<18} {'Est. Distância':<15}")
            print("-" * 80)

            for i, (gen, fitness) in enumerate(zip(generations, fitness_values)):
                # Estimar violações assumindo WEIGHT=1000 e distância base ~1800
                estimated_violations = max(0, (fitness - 1800) / 1000)
                estimated_distance = fitness - (estimated_violations * 1000)

                # Mostrar apenas pontos chave
                if gen % 300 == 0 or gen == generations[-1]:
                    print(
                        f"{gen:<10} {fitness:<15.2f} {estimated_violations:<18.1f} {estimated_distance:<15.2f}")

            print()
            print("ANÁLISE:")
            print("-" * 80)

            # Melhorias
            initial = fitness_values[0]
            final = fitness_values[-1]
            improvement = initial - final
            improvement_pct = (improvement / initial) * 100

            print(f"Fitness inicial:  {initial:.2f}")
            print(f"Fitness final:    {final:.2f}")
            print(
                f"Melhoria:         {improvement:.2f} ({improvement_pct:.1f}%)")
            print()

            # Estimativas finais
            final_violations = max(0, (final - 1800) / 1000)
            final_distance = final - (final_violations * 1000)

            print(f"Estimativa final:")
            print(f"  • Distância:  ~{final_distance:.0f}")
            print(f"  • Violações:  ~{final_violations:.0f}")
            print(f"  • Penalidade: ~{final_violations * 1000:.0f}")
            print()

            # Velocidade de convergência
            half_point = len(fitness_values) // 2
            first_half_improv = fitness_values[0] - fitness_values[half_point]
            second_half_improv = fitness_values[half_point] - \
                fitness_values[-1]

            print(f"Velocidade de convergência:")
            print(f"  • Primeira metade (0-1500): -{first_half_improv:.2f}")
            print(f"  • Segunda metade (1500-3000): -{second_half_improv:.2f}")

            if second_half_improv < first_half_improv * 0.3:
                print(f"  ⚠️  ALERTA: Convergência prematura detectada!")
                print(
                    f"      AG evoluiu pouco na segunda metade ({second_half_improv:.0f} vs {first_half_improv:.0f})")

            break

    print()
    print("=" * 80)
    print("RECOMENDAÇÕES:")
    print("=" * 80)

    if final_violations > 15:
        print("❌ Muitas violações ainda presentes (>15)")
        print("   Sugestões:")
        print("   1. Aumentar número de gerações (3000 → 5000)")
        print("   2. Implementar operador de busca local focado em janelas de tempo")
        print("   3. Ajustar taxa de mutação inter-rota")
    elif final_violations > 5:
        print("⚠️  Violações moderadas (5-15)")
        print("   Sugestões:")
        print("   1. Aumentar número de gerações")
        print("   2. Ajustar parâmetros de crossover")
    else:
        print("✅ Poucas violações (<5) - AG está funcionando bem!")

    print()


if __name__ == '__main__':
    analyze_evolution('results_validation_C1/C101/evo_c101_exec01.txt')
