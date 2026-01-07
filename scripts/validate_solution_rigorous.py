#!/usr/bin/env python3
"""
Validador RIGOROSO de soluções VRPTW
Verifica TODAS as constraints e detecta violações
"""

import sys
import os
import re
from pathlib import Path
import math

class Customer:
    def __init__(self, id, x, y, demand, ready_time, due_time, service_time):
        self.id = id
        self.x = x
        self.y = y
        self.demand = demand
        self.ready_time = ready_time
        self.due_time = due_time
        self.service_time = service_time

class Instance:
    def __init__(self, filename):
        self.filename = filename
        self.customers = []
        self.vehicle_capacity = 0
        self.num_vehicles = 0
        self.load_instance(filename)
    
    def load_instance(self, filename):
        """Carrega a instância Solomon"""
        with open(filename, 'r') as f:
            lines = f.readlines()
        
        # Pula as primeiras 4 linhas (header)
        # Linha 5: nome, linha 6: vazio, linha 7: vazio, linha 8: vazio
        # Linha 9: informações dos veículos
        vehicle_line = lines[4].split()
        self.num_vehicles = int(vehicle_line[0])
        self.vehicle_capacity = int(vehicle_line[1])
        
        # Clientes começam na linha 9 (índice 8) após pular header de clientes
        customer_start = 9
        
        for line in lines[customer_start:]:
            parts = line.split()
            if len(parts) < 7:
                continue
            
            customer = Customer(
                id=int(parts[0]),
                x=float(parts[1]),
                y=float(parts[2]),
                demand=int(parts[3]),
                ready_time=int(parts[4]),
                due_time=int(parts[5]),
                service_time=int(parts[6])
            )
            self.customers.append(customer)
    
    def get_customer(self, id):
        """Retorna o cliente com o ID especificado"""
        for c in self.customers:
            if c.id == id:
                return c
        return None
    
    def distance(self, c1, c2):
        """Calcula distância euclidiana entre dois clientes"""
        dx = c1.x - c2.x
        dy = c1.y - c2.y
        return math.sqrt(dx * dx + dy * dy)


class SolutionValidator:
    def __init__(self, instance, solution_file):
        self.instance = instance
        self.solution_file = solution_file
        self.routes = []
        self.errors = []
        self.warnings = []
        self.total_distance = 0
        self.num_vehicles_used = 0
        
    def load_solution(self):
        """Carrega a solução do arquivo"""
        with open(self.solution_file, 'r') as f:
            content = f.read()
        
        # Procura a seção de ROTAS FINAIS
        if 'ROTAS FINAIS' in content:
            # Pega apenas a seção final
            final_section = content.split('ROTAS FINAIS')[1]
        else:
            # Caso não tenha, usa o arquivo todo
            final_section = content
        
        lines = final_section.split('\n')
        
        # Procura linhas que começam com "Veículo"
        for line in lines:
            line = line.strip()
            
            if line.startswith('Veículo'):
                # Formato: Veículo X: Depósito(0) -> Cliente(13) -> ... -> Depósito(0)
                # Extrai todos os números entre parênteses
                matches = re.findall(r'Cliente\((\d+)\)', line)
                
                if matches:
                    route = [int(n) for n in matches]
                    self.routes.append(route)
        
        self.num_vehicles_used = len(self.routes)
        
        if not self.routes:
            self.errors.append("ERRO: Nenhuma rota encontrada no arquivo de solução!")
            return False
        
        return True
    
    def validate_all_customers_visited(self):
        """Verifica se todos os clientes foram visitados exatamente uma vez"""
        visited = set()
        duplicates = set()
        
        for route_idx, route in enumerate(self.routes):
            for customer_id in route:
                if customer_id == 0:  # Depot não conta
                    continue
                
                if customer_id in visited:
                    duplicates.add(customer_id)
                    self.errors.append(f"ERRO: Cliente {customer_id} visitado múltiplas vezes!")
                
                visited.add(customer_id)
        
        # Verifica clientes não visitados (exceto depot 0)
        all_customers = set(c.id for c in self.instance.customers if c.id != 0)
        not_visited = all_customers - visited
        
        if not_visited:
            self.errors.append(f"ERRO: {len(not_visited)} clientes NÃO visitados: {sorted(list(not_visited))}")
        
        return len(duplicates) == 0 and len(not_visited) == 0
    
    def validate_capacity(self):
        """Verifica se a capacidade dos veículos é respeitada"""
        all_valid = True
        
        for route_idx, route in enumerate(self.routes):
            total_demand = 0
            
            for customer_id in route:
                customer = self.instance.get_customer(customer_id)
                if customer:
                    total_demand += customer.demand
            
            if total_demand > self.instance.vehicle_capacity:
                self.errors.append(
                    f"ERRO: Rota {route_idx+1} excede capacidade! "
                    f"Demanda: {total_demand}, Capacidade: {self.instance.vehicle_capacity}"
                )
                all_valid = False
        
        return all_valid
    
    def validate_time_windows(self):
        """Verifica se as janelas de tempo são respeitadas"""
        all_valid = True
        depot = self.instance.get_customer(0)
        
        for route_idx, route in enumerate(self.routes):
            current_time = depot.ready_time
            current_customer = depot
            
            for customer_id in route:
                customer = self.instance.get_customer(customer_id)
                if not customer:
                    self.errors.append(f"ERRO: Cliente {customer_id} não existe na instância!")
                    all_valid = False
                    continue
                
                # Tempo de viagem
                travel_time = self.instance.distance(current_customer, customer)
                arrival_time = current_time + travel_time
                
                # Se chegar antes da janela, espera
                start_service = max(arrival_time, customer.ready_time)
                
                # Verifica se chegou depois do deadline
                if arrival_time > customer.due_time:
                    self.errors.append(
                        f"ERRO: Rota {route_idx+1} - Cliente {customer_id} "
                        f"visitado FORA da janela de tempo! "
                        f"Chegada: {arrival_time:.2f}, Deadline: {customer.due_time}"
                    )
                    all_valid = False
                
                # Atualiza tempo (início do serviço + tempo de serviço)
                current_time = start_service + customer.service_time
                current_customer = customer
            
            # Retorno ao depot
            travel_time = self.instance.distance(current_customer, depot)
            arrival_time = current_time + travel_time
            
            if arrival_time > depot.due_time:
                self.warnings.append(
                    f"AVISO: Rota {route_idx+1} retorna ao depot FORA da janela! "
                    f"Chegada: {arrival_time:.2f}, Deadline: {depot.due_time}"
                )
        
        return all_valid
    
    def calculate_total_distance(self):
        """Calcula a distância total percorrida"""
        total = 0
        depot = self.instance.get_customer(0)
        
        for route in self.routes:
            if not route:
                continue
            
            # Depot -> primeiro cliente
            first_customer = self.instance.get_customer(route[0])
            route_distance = self.instance.distance(depot, first_customer)
            
            # Clientes da rota
            for i in range(len(route) - 1):
                c1 = self.instance.get_customer(route[i])
                c2 = self.instance.get_customer(route[i+1])
                route_distance += self.instance.distance(c1, c2)
            
            # Último cliente -> depot
            last_customer = self.instance.get_customer(route[-1])
            route_distance += self.instance.distance(last_customer, depot)
            
            total += route_distance
        
        self.total_distance = total
        return total
    
    def validate(self):
        """Executa todas as validações"""
        print("=" * 80)
        print(f"VALIDAÇÃO RIGOROSA DE SOLUÇÃO")
        print(f"Instância: {os.path.basename(self.instance.filename)}")
        print(f"Solução: {os.path.basename(self.solution_file)}")
        print("=" * 80)
        print()
        
        # Carrega solução
        if not self.load_solution():
            print("❌ FALHA ao carregar solução!")
            return False
        
        print(f"✓ Solução carregada: {self.num_vehicles_used} veículos")
        print()
        
        # Validações
        print("🔍 Validando cobertura de clientes...")
        coverage_ok = self.validate_all_customers_visited()
        if coverage_ok:
            print("  ✓ Todos os clientes visitados exatamente uma vez")
        else:
            print("  ❌ PROBLEMA na cobertura de clientes!")
        print()
        
        print("🔍 Validando capacidade dos veículos...")
        capacity_ok = self.validate_capacity()
        if capacity_ok:
            print(f"  ✓ Capacidade respeitada (limite: {self.instance.vehicle_capacity})")
        else:
            print("  ❌ VIOLAÇÃO de capacidade detectada!")
        print()
        
        print("🔍 Validando janelas de tempo...")
        time_ok = self.validate_time_windows()
        if time_ok:
            print("  ✓ Janelas de tempo respeitadas")
        else:
            print("  ❌ VIOLAÇÃO de janelas de tempo detectada!")
        print()
        
        # Calcula distância
        distance = self.calculate_total_distance()
        print(f"📏 Distância total: {distance:.2f}")
        print(f"🚚 Veículos usados: {self.num_vehicles_used}")
        print()
        
        # Resumo
        print("=" * 80)
        if self.errors:
            print(f"❌ SOLUÇÃO INVÁLIDA - {len(self.errors)} ERRO(S) ENCONTRADO(S):")
            print()
            for error in self.errors:
                print(f"  • {error}")
            print()
        
        if self.warnings:
            print(f"⚠️  {len(self.warnings)} AVISO(S):")
            print()
            for warning in self.warnings:
                print(f"  • {warning}")
            print()
        
        if not self.errors and not self.warnings:
            print("✅ SOLUÇÃO VÁLIDA!")
            print(f"   Distância: {distance:.2f}")
            print(f"   Veículos: {self.num_vehicles_used}")
        
        print("=" * 80)
        
        return len(self.errors) == 0


def main():
    if len(sys.argv) < 3:
        print("Uso: python3 validate_solution_rigorous.py <instance_file> <solution_file>")
        print()
        print("Exemplo:")
        print("  python3 validate_solution_rigorous.py src/instances/solomon/C101.txt results_validation_C1/C101/evo_c101_exec01.txt")
        sys.exit(1)
    
    instance_file = sys.argv[1]
    solution_file = sys.argv[2]
    
    if not os.path.exists(instance_file):
        print(f"❌ Arquivo de instância não encontrado: {instance_file}")
        sys.exit(1)
    
    if not os.path.exists(solution_file):
        print(f"❌ Arquivo de solução não encontrado: {solution_file}")
        sys.exit(1)
    
    # Carrega instância
    instance = Instance(instance_file)
    
    # Valida solução
    validator = SolutionValidator(instance, solution_file)
    is_valid = validator.validate()
    
    sys.exit(0 if is_valid else 1)


if __name__ == '__main__':
    main()
