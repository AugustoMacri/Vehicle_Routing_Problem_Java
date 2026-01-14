#!/usr/bin/env python3
import re
import os


def count_clients_in_solution(filepath):
    """Conta quantos clientes únicos estão presentes na solução"""
    try:
        with open(filepath, 'r') as f:
            content = f.read()
        clients = re.findall(r'Cliente\((\d+)\)', content)
        unique_clients = set(int(c) for c in clients)
        return len(unique_clients)
    except FileNotFoundError:
        return None


# Verificar instâncias
print("Verificando cobertura de clientes (deveria ser 100 para todas):\n")

print("Instâncias C1:")
for i in range(101, 110):
    inst = f"C{i}"
    filepath = f"results_validation_C1/{inst}/evo_c{i}_exec01.txt"
    count = count_clients_in_solution(filepath)
    status = "✅" if count == 100 else "❌"
    print(f"  {inst}: {status} {count if count else 'N/A'}/100 clientes")

print("\nInstâncias R1:")
for i in range(101, 110):
    inst = f"R{i}"
    filepath = f"results_validation_R1/{inst}/evo_r{i}_exec01.txt"
    count = count_clients_in_solution(filepath)
    status = "✅" if count == 100 else "❌"
    print(f"  {inst}: {status} {count if count else 'N/A'}/100 clientes")

print("\nInstâncias RC1:")
for i in range(101, 109):
    inst = f"RC{i}"
    filepath = f"results_validation_RC1/{inst}/evo_rc{i}_exec01.txt"
    count = count_clients_in_solution(filepath)
    status = "✅" if count == 100 else "❌"
    print(f"  {inst}: {status} {count if count else 'N/A'}/100 clientes")
