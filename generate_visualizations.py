#!/usr/bin/env python3
"""
Script para gerar gráficos comparativos dos resultados de validação
"""

import matplotlib.pyplot as plt
import numpy as np
from pathlib import Path

# Dados consolidados dos resultados
C1_RESULTS = {
    'C101': {'best': 1260.16, 'avg': 1264.15, 'bk': 828.94, 'gap': 52.02},
    'C102': {'best': 1298.93, 'avg': 1316.40, 'bk': 828.94, 'gap': 56.70},
    'C103': {'best': 1401.71, 'avg': 1420.55, 'bk': 828.06, 'gap': 69.28},
    'C104': {'best': 1236.74, 'avg': 1253.93, 'bk': 824.78, 'gap': 49.95},
    'C105': {'best': 1372.32, 'avg': 1386.58, 'bk': 828.94, 'gap': 65.55},
    'C106': {'best': 1328.70, 'avg': 1361.80, 'bk': 828.94, 'gap': 60.29},
    'C107': {'best': 1306.28, 'avg': 1381.20, 'bk': 828.94, 'gap': 57.58},
    'C108': {'best': 1268.72, 'avg': 1320.09, 'bk': 828.94, 'gap': 53.05},
    'C109': {'best': 1264.51, 'avg': 1292.53, 'bk': 828.94, 'gap': 52.55},
}

R1_RESULTS = {
    'R101': {'best': 1944.56, 'avg': 1959.05, 'bk': 1650.80, 'gap': 17.80},
    'R102': {'best': 1862.84, 'avg': 1883.15, 'bk': 1486.12, 'gap': 25.35},
    'R103': {'best': 1613.87, 'avg': 1613.87, 'bk': 1292.68, 'gap': 24.85},
    'R104': {'best': 1296.73, 'avg': 1317.62, 'bk': 1007.31, 'gap': 28.73},
    'R105': {'best': 1706.78, 'avg': 1728.57, 'bk': 1377.11, 'gap': 23.94},
    'R106': {'best': 1573.49, 'avg': 1598.73, 'bk': 1252.03, 'gap': 25.68},
    'R107': {'best': 1400.69, 'avg': 1412.42, 'bk': 1104.66, 'gap': 26.80},
    'R108': {'best': 1238.95, 'avg': 1257.27, 'bk': 960.88, 'gap': 28.94},
    'R109': {'best': 1610.99, 'avg': 1637.26, 'bk': 1194.73, 'gap': 34.84},
}

RC1_RESULTS = {
    'RC101': {'best': 2192.90, 'avg': 2236.98, 'bk': 1696.95, 'gap': 29.23},
    'RC102': {'best': 1888.57, 'avg': 1904.30, 'bk': 1554.75, 'gap': 21.47},
    'RC103': {'best': 1628.71, 'avg': 1667.19, 'bk': 1261.67, 'gap': 29.09},
    'RC104': {'best': 1446.21, 'avg': 1474.08, 'bk': 1135.48, 'gap': 27.37},
    'RC105': {'best': 1875.01, 'avg': 1911.07, 'bk': 1629.44, 'gap': 15.07},
    'RC106': {'best': 1836.61, 'avg': 1879.26, 'bk': 1424.73, 'gap': 28.91},
    'RC107': {'best': 1744.98, 'avg': 1782.04, 'bk': 1230.48, 'gap': 41.81},
    'RC108': {'best': 1536.05, 'avg': 1548.49, 'bk': 1139.82, 'gap': 34.76},
}


def plot_gap_comparison():
    """Gráfico de comparação de gaps entre classes"""
    fig, ax = plt.subplots(figsize=(14, 8))

    # Preparar dados
    all_instances = list(C1_RESULTS.keys()) + \
        list(R1_RESULTS.keys()) + list(RC1_RESULTS.keys())
    all_gaps = [C1_RESULTS[k]['gap'] for k in C1_RESULTS.keys()] + \
               [R1_RESULTS[k]['gap'] for k in R1_RESULTS.keys()] + \
               [RC1_RESULTS[k]['gap'] for k in RC1_RESULTS.keys()]

    # Cores por classe
    colors = ['#FF6B6B'] * len(C1_RESULTS) + \
             ['#4ECDC4'] * len(R1_RESULTS) + \
             ['#95E1D3'] * len(RC1_RESULTS)

    # Plotar barras
    bars = ax.bar(range(len(all_instances)), all_gaps, color=colors, alpha=0.8)

    # Adicionar linha de referência para médias
    ax.axhline(y=57.44, color='#FF6B6B', linestyle='--',
               linewidth=2, label='Média C1: 57.44%')
    ax.axhline(y=27.33, color='#4ECDC4', linestyle='--',
               linewidth=2, label='Média R1: 27.33%')
    ax.axhline(y=28.46, color='#95E1D3', linestyle='--',
               linewidth=2, label='Média RC1: 28.46%')

    # Configurações
    ax.set_xlabel('Instância', fontsize=12, fontweight='bold')
    ax.set_ylabel('Gap em relação ao Best-Known (%)',
                  fontsize=12, fontweight='bold')
    ax.set_title('Comparação de Gaps: AEMMT vs Best-Known Solomon\n(Janeiro 2026 - 100% Soluções Válidas)',
                 fontsize=14, fontweight='bold')
    ax.set_xticks(range(len(all_instances)))
    ax.set_xticklabels(all_instances, rotation=45, ha='right')
    ax.legend(fontsize=10)
    ax.grid(axis='y', alpha=0.3)

    # Adicionar valores nas barras
    for i, (bar, gap) in enumerate(zip(bars, all_gaps)):
        height = bar.get_height()
        ax.text(bar.get_x() + bar.get_width()/2., height,
                f'{gap:.1f}%',
                ha='center', va='bottom', fontsize=8, fontweight='bold')

    plt.tight_layout()
    plt.savefig('gap_comparison_all_instances.png',
                dpi=300, bbox_inches='tight')
    print("✓ Gráfico de gaps salvo: gap_comparison_all_instances.png")
    plt.close()


def plot_distance_comparison():
    """Gráfico comparando distâncias obtidas vs best-known"""
    fig, axes = plt.subplots(1, 3, figsize=(18, 6))

    datasets = [
        (C1_RESULTS, 'Classe C1 (Clustered)', '#FF6B6B', axes[0]),
        (R1_RESULTS, 'Classe R1 (Random)', '#4ECDC4', axes[1]),
        (RC1_RESULTS, 'Classe RC1 (Random-Clustered)', '#95E1D3', axes[2])
    ]

    for data, title, color, ax in datasets:
        instances = list(data.keys())
        x = np.arange(len(instances))
        width = 0.35

        bk_values = [data[k]['bk'] for k in instances]
        best_values = [data[k]['best'] for k in instances]

        bars1 = ax.bar(x - width/2, bk_values, width,
                       label='Best-Known', color='gray', alpha=0.7)
        bars2 = ax.bar(x + width/2, best_values, width,
                       label='AEMMT (Melhor)', color=color, alpha=0.8)

        ax.set_xlabel('Instância', fontweight='bold')
        ax.set_ylabel('Distância', fontweight='bold')
        ax.set_title(title, fontweight='bold')
        ax.set_xticks(x)
        ax.set_xticklabels(instances, rotation=45, ha='right')
        ax.legend()
        ax.grid(axis='y', alpha=0.3)

    plt.suptitle('Comparação de Distâncias: AEMMT vs Best-Known',
                 fontsize=16, fontweight='bold', y=1.02)
    plt.tight_layout()
    plt.savefig('distance_comparison_by_class.png',
                dpi=300, bbox_inches='tight')
    print("✓ Gráfico de distâncias salvo: distance_comparison_by_class.png")
    plt.close()


def plot_gap_distribution():
    """Histograma da distribuição de gaps"""
    fig, ax = plt.subplots(figsize=(10, 6))

    c1_gaps = [v['gap'] for v in C1_RESULTS.values()]
    r1_gaps = [v['gap'] for v in R1_RESULTS.values()]
    rc1_gaps = [v['gap'] for v in RC1_RESULTS.values()]

    bins = np.arange(0, 80, 5)

    ax.hist(c1_gaps, bins=bins, alpha=0.6, label='C1',
            color='#FF6B6B', edgecolor='black')
    ax.hist(r1_gaps, bins=bins, alpha=0.6, label='R1',
            color='#4ECDC4', edgecolor='black')
    ax.hist(rc1_gaps, bins=bins, alpha=0.6, label='RC1',
            color='#95E1D3', edgecolor='black')

    ax.set_xlabel('Gap (%)', fontsize=12, fontweight='bold')
    ax.set_ylabel('Número de Instâncias', fontsize=12, fontweight='bold')
    ax.set_title('Distribuição de Gaps por Classe de Instância',
                 fontsize=14, fontweight='bold')
    ax.legend(fontsize=10)
    ax.grid(axis='y', alpha=0.3)

    plt.tight_layout()
    plt.savefig('gap_distribution.png', dpi=300, bbox_inches='tight')
    print("✓ Histograma de distribuição salvo: gap_distribution.png")
    plt.close()


def plot_summary_statistics():
    """Gráfico resumo com estatísticas por classe"""
    fig, ax = plt.subplots(figsize=(10, 6))

    classes = ['C1', 'R1', 'RC1']
    avg_gaps = [57.44, 27.33, 28.46]
    min_gaps = [49.95, 17.80, 15.07]
    max_gaps = [69.28, 34.84, 41.81]

    x = np.arange(len(classes))
    width = 0.6

    bars = ax.bar(x, avg_gaps, width, label='Gap Médio', color=[
                  '#FF6B6B', '#4ECDC4', '#95E1D3'], alpha=0.8)

    # Adicionar range com linhas verticais
    for i, (min_g, max_g, avg_g) in enumerate(zip(min_gaps, max_gaps, avg_gaps)):
        ax.plot([i, i], [min_g, max_g], color='black',
                linewidth=2, marker='_', markersize=15)
        ax.text(i, max_g + 2, f'{max_g:.1f}%',
                ha='center', fontsize=9, fontweight='bold')
        ax.text(i, min_g - 2, f'{min_g:.1f}%',
                ha='center', fontsize=9, fontweight='bold')

    # Adicionar valores das médias
    for i, (bar, avg) in enumerate(zip(bars, avg_gaps)):
        ax.text(bar.get_x() + bar.get_width()/2., avg/2,
                f'{avg:.2f}%',
                ha='center', va='center', fontsize=14, fontweight='bold', color='white')

    ax.set_ylabel('Gap (%)', fontsize=12, fontweight='bold')
    ax.set_title('Resumo Estatístico: Gap por Classe de Instância\n(Média com Min-Max)',
                 fontsize=14, fontweight='bold')
    ax.set_xticks(x)
    ax.set_xticklabels(classes, fontsize=12, fontweight='bold')
    ax.set_ylim(0, 80)
    ax.grid(axis='y', alpha=0.3)

    plt.tight_layout()
    plt.savefig('summary_statistics.png', dpi=300, bbox_inches='tight')
    print("✓ Gráfico de estatísticas salvo: summary_statistics.png")
    plt.close()


if __name__ == "__main__":
    print("\n" + "="*60)
    print("GERANDO VISUALIZAÇÕES DOS RESULTADOS")
    print("="*60 + "\n")

    plot_gap_comparison()
    plot_distance_comparison()
    plot_gap_distribution()
    plot_summary_statistics()

    print("\n" + "="*60)
    print("✅ Todas as visualizações foram geradas com sucesso!")
    print("="*60 + "\n")
    print("Arquivos gerados:")
    print("  1. gap_comparison_all_instances.png")
    print("  2. distance_comparison_by_class.png")
    print("  3. gap_distribution.png")
    print("  4. summary_statistics.png")
    print()
