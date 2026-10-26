
import type { FormEvent } from 'react';

import { useEffect, useMemo, useState } from 'react';

import {
    LayoutDashboard,
    Package,
    ArrowLeftRight,
    Bell,
    ChartNoAxesCombined,
    Settings,
    Search,
    Plus,
    Leaf,
    LogOut,
    RefreshCw,
    AlertTriangle,
    TrendingUp,
    Wallet,
    Boxes,
    ChevronRight,
    X,
    Menu,
    Download,
    CalendarDays,
    CheckCircle2,
} from 'lucide-react';

import {
    ResponsiveContainer,
    AreaChart,
    Area,
    XAxis,
    YAxis,
    Tooltip,
    CartesianGrid,
    BarChart,
    Bar,
} from 'recharts';

type Supply = {
    id: string;
    name: string;
    category: string;
    unit: string;
    quantity: number;
    minimumStock: number;
    supplier?: string;
    location?: string;
    unitCost?: number | null;
};

type Alert = {
    id: string;
    supplyId: string;
    supplyName: string;
    type: string;
    priority: string;
    explanation: string;
    recommendation: string;
    status: string;
};

type Movement = {
    id: string;
    supplyName: string;
    type: string;
    quantity: number;
    unit: string;
    movementDate: string;
    responsible?: string;
    counterparty?: string;
    notes?: string;
};

const API =
    import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const money = (value: number) =>
    new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL',
        maximumFractionDigits: 0,
    }).format(value);

const number = (value: number) =>
    new Intl.NumberFormat('pt-BR', {
        maximumFractionDigits: 2,
    }).format(value);

const date = (value: string) =>
    value
        ? new Date(`${value}T12:00:00`).toLocaleDateString('pt-BR')
        : '—';

const demoSupplies: Supply[] = [
    {
        id: 'd1',
        name: 'Ureia agrícola',
        category: 'Fertilizantes',
        unit: 'kg',
        quantity: 1200,
        minimumStock: 1000,
        supplier: 'Agro Norte',
        location: 'Galpão A',
        unitCost: 3.45,
    },
    {
        id: 'd2',
        name: 'Sementes de milho híbrido',
        category: 'Sementes',
        unit: 'sacas',
        quantity: 18,
        minimumStock: 25,
        supplier: 'Sementes Brasil',
        location: 'Depósito 2',
        unitCost: 780,
    },
    {
        id: 'd3',
        name: 'Defensivo biológico',
        category: 'Produtos biológicos',
        unit: 'L',
        quantity: 42,
        minimumStock: 10,
        supplier: 'BioCampo',
        location: 'Câmara fresca',
        unitCost: 96,
    },
    {
        id: 'd4',
        name: 'Óleo diesel S10',
        category: 'Combustíveis',
        unit: 'L',
        quantity: 320,
        minimumStock: 150,
        supplier: 'Posto Rural',
        location: 'Tanque externo',
        unitCost: 6.18,
    },
    {
        id: 'd5',
        name: 'Fertilizante NPK 04-14-08',
        category: 'Fertilizantes',
        unit: 'kg',
        quantity: 2400,
        minimumStock: 500,
        supplier: 'Agro Norte',
        location: 'Galpão B',
        unitCost: 4.2,
    },
];

const demoAlerts: Alert[] = [
    {
        id: 'a1',
        supplyId: 'd2',
        supplyName: 'Sementes de milho híbrido',
        type: 'ESTOQUE_MINIMO',
        priority: 'ATENCAO',
        explanation:
            'Saldo atual: 18 sacas; mínimo configurado: 25 sacas.',
        recommendation:
            'Conferir o saldo físico e avaliar reposição.',
        status: 'NOVO',
    },
    {
        id: 'a2',
        supplyId: 'd1',
        supplyName: 'Ureia agrícola',
        type: 'RISCO_RUPTURA',
        priority: 'CRITICO',
        explanation:
            'Cenário demonstrativo: consumo recente pode reduzir a cobertura. Confirme o histórico antes de decidir.',
        recommendation:
            'Avaliar reposição e revisar o histórico de consumo.',
        status: 'NOVO',
    },
];

const demoMovements: Movement[] = [
    {
        id: 'm1',
        supplyName: 'Ureia agrícola',
        type: 'SAIDA',
        quantity: 350,
        unit: 'kg',
        movementDate: '2026-10-24',
        responsible: 'João Silva',
        counterparty: 'Talhão 04',
    },
    {
        id: 'm2',
        supplyName: 'Óleo diesel S10',
        type: 'ENTRADA',
        quantity: 500,
        unit: 'L',
        movementDate: '2026-10-22',
        responsible: 'Maria Souza',
        counterparty: 'Posto Rural',
    },
    {
        id: 'm3',
        supplyName: 'Sementes de milho híbrido',
        type: 'SAIDA',
        quantity: 7,
        unit: 'sacas',
        movementDate: '2026-10-20',
        responsible: 'João Silva',
        counterparty: 'Plantio norte',
    },
];
