import os
import glob
import matplotlib.pyplot as plt
import numpy as np


def list_instances():
    """Lista as instâncias disponíveis e permite escolher uma"""
    instance_dir = "src/instances/solomon/"
    # Verifica se o diretório existe
    if not os.path.exists(instance_dir):
        instance_dir = "bin/instances/solomon/"  # Tenta outra localização comum
        if not os.path.exists(instance_dir):
            print("Diretório de instâncias não encontrado.")
            return None

    # Lista todos os arquivos .txt no diretório
    instance_files = sorted(glob.glob(os.path.join(instance_dir, "*.txt")))

    if not instance_files:
        print("Nenhuma instância encontrada.")
        return None

    # Mostra as instâncias disponíveis
    print("\nInstâncias disponíveis:")
    for i, file_path in enumerate(instance_files):
        file_name = os.path.basename(file_path)
        print(f"{i+1}. {file_name}")

    # Solicita a escolha do usuário
    choice = -1
    while choice < 1 or choice > len(instance_files):
        try:
            choice = int(
                input(f"\nEscolha uma instância (1-{len(instance_files)}): "))
            if choice < 1 or choice > len(instance_files):
                print(
                    f"Por favor, escolha um número entre 1 e {len(instance_files)}.")
        except ValueError:
            print("Por favor, digite um número válido.")

    return instance_files[choice-1]


def read_instance_coordinates(file_path):
    """Lê as coordenadas dos clientes de um arquivo de instância"""
    coordinates = []

    try:
        with open(file_path, 'r') as f:
            lines = f.readlines()

        # Procura a seção CUSTOMER
        customer_section = False
        header_passed = False

        for line in lines:
            line = line.strip()

            # Identifica o início da seção CUSTOMER
            if "CUSTOMER" in line:
                customer_section = True
                continue

            # Pula linhas de cabeçalho
            if customer_section and not header_passed:
                if "CUST NO." in line:
                    header_passed = True
                continue

            # Processa linhas de dados
            if customer_section and header_passed and line:
                # Divide a linha em partes, ignorando espaços extras
                parts = line.split()
                if len(parts) >= 3:  # Verifica se há pelo menos cust_no, x, y
                    try:
                        # O formato esperado é: CUST_NO X_COORD Y_COORD ...
                        cust_no = int(parts[0])
                        x_coord = float(parts[1])
                        y_coord = float(parts[2])
                        coordinates.append((cust_no, x_coord, y_coord))
                    except (ValueError, IndexError):
                        # Ignora linhas que não podem ser convertidas
                        pass

    except Exception as e:
        print(f"Erro ao ler o arquivo {file_path}: {e}")

    return coordinates


def plot_coordinates(coordinates, instance_name):
    """Plota as coordenadas em um plano cartesiano e salva na pasta results_mapping"""
    if not coordinates:
        print("Nenhuma coordenada encontrada para plotar.")
        return

    # Extrair coordenadas para plotagem
    cust_nos = [c[0] for c in coordinates]
    x_coords = [c[1] for c in coordinates]
    y_coords = [c[2] for c in coordinates]

    # Destacar o depósito (cliente 0)
    depot_index = cust_nos.index(0) if 0 in cust_nos else None

    # Configurar o plot
    plt.figure(figsize=(10, 8))

    # Plotar todos os clientes
    plt.scatter(x_coords, y_coords, c='blue', marker='o', s=50, alpha=0.7)

    # Destacar o depósito
    if depot_index is not None:
        plt.scatter(x_coords[depot_index], y_coords[depot_index],
                    c='red', marker='s', s=100, label='Depósito')

    # Configurações do gráfico
    plt.title(f'Mapa de Clientes - Instância {instance_name}', fontsize=16)
    plt.xlabel('Coordenada X', fontsize=12)
    plt.ylabel('Coordenada Y', fontsize=12)
    plt.grid(True, linestyle='--', alpha=0.7)

    # Adicionar legenda se houver depósito
    if depot_index is not None:
        plt.legend()

    # Ajustar limites para visualização adequada (margem de 10%)
    x_min, x_max = min(x_coords), max(x_coords)
    y_min, y_max = min(y_coords), max(y_coords)
    x_margin = (x_max - x_min) * 0.1
    y_margin = (y_max - y_min) * 0.1

    plt.xlim(x_min - x_margin, x_max + x_margin)
    plt.ylim(y_min - y_margin, y_max + y_margin)

    # Criar diretório para salvar o mapa se não existir
    save_dir = "mapping_results"
    os.makedirs(save_dir, exist_ok=True)

    # Gerar nome de arquivo com timestamp
    timestamp = plt.matplotlib.dates.date2num(
        plt.matplotlib.dates.datetime.datetime.now())
    timestamp_str = plt.matplotlib.dates.num2date(
        timestamp).strftime("%Y%m%d_%H%M%S")
    save_path = os.path.join(
        save_dir, f"map_{instance_name}_{timestamp_str}.png")

    # Salvar a figura
    plt.tight_layout()
    plt.savefig(save_path, dpi=300, bbox_inches='tight')
    print(f"Mapa salvo em: {save_path}")



def main():
    print("=== Visualizador de Instâncias VRP ===")

    # Listar e escolher instância
    instance_path = list_instances()

    if not instance_path:
        return

    # Extrair nome da instância do caminho do arquivo
    instance_name = os.path.basename(instance_path).replace('.txt', '')

    print(f"\nLendo coordenadas da instância {instance_name}...")
    coordinates = read_instance_coordinates(instance_path)

    if coordinates:
        print(f"Encontrados {len(coordinates)} pontos.")
        plot_coordinates(coordinates, instance_name)
    else:
        print("Não foi possível extrair coordenadas da instância.")


if __name__ == "__main__":
    main()
