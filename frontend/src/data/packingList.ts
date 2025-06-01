import { PackingCategory } from '../types';

export const packingCategories: PackingCategory[] = [
  {
    id: 'cat-1', // ID único para la categoría
    title: 'Ropa',
    items: [
      { id: 'item-1-1', text: 'Mochila anatómica' },
      { id: 'item-1-2', text: 'Cuello polar' },
      { id: 'item-1-3', text: 'Gorro para el sol' },
      { id: 'item-1-4', text: 'Buff' },
      { id: 'item-1-5', text: 'Gorro de polar' },
      { id: 'item-1-6', text: 'Lentes de sol' },
      { id: 'item-1-7', text: 'Campera rompevientos' },
      { id: 'item-1-8', text: 'Guantes de abrigo' },
      { id: 'item-1-9', text: '6 Remeras deportivas' },
      { id: 'item-1-10', text: '2 Remeras térmicas' },
      { id: 'item-1-11', text: '2 Buzos (se recomienda micropolar)' },
      { id: 'item-1-12', text: '2 Pantalones largos' },
      { id: 'item-1-13', text: '2 Calzas largas (opcional)' },
      { id: 'item-1-14', text: '2 Pantalones cortos' },
      { id: 'item-1-15', text: '1 Malla / Short de baño' },
      { id: 'item-1-16', text: '6 Mudas de ropa interior' },
      { id: 'item-1-17', text: '6 Pares de medias deportivas o de trekking (se recomienda tipo fútbol)' },
      { id: 'item-1-18', text: '1 Par de medias de abrigo' },
      { id: 'item-1-19', text: 'Botas o zapatillas de trekking' },
      { id: 'item-1-20', text: '1 Calzado de descanso (zapatillas deportivas comunes)' },
      { id: 'item-1-21', text: 'Calzado para agua (crocs, sandalias)' },
    ],
  },
  {
    id: 'cat-2',
    title: 'Documentos y Seguridad',
    items: [
      { id: 'item-2-1', text: 'DNI y carnet de la obra social' },
      { id: 'item-2-2', text: '1 Silbato' },
      { id: 'item-2-3', text: '1 Par de cordones extra' },
      { id: 'item-2-4', text: '1 Linterna (frontal o manual)' },
      { id: 'item-2-5', text: '1 Cubre mochila' },
    ],
  },
  {
    id: 'cat-3',
    title: 'Hidratación y Salud',
    items: [
      { id: 'item-3-1', text: '1 Botella plástica de 2L' },
      { id: 'item-3-2', text: 'Botiquín personal' },
      { id: 'item-3-3', text: 'Silvertape' },
    ],
  },
  {
    id: 'cat-4',
    title: 'Campamento',
    items: [
      { id: 'item-4-1', text: 'Bolsa de dormir (rango térmico consultar con los responsables)' },
      { id: 'item-4-2', text: 'Aislante' },
      { id: 'item-4-3', text: 'Soga para colgar ropa' },
      { id: 'item-4-4', text: 'Broches de ropa' },
      { id: 'item-4-5', text: 'Mochila de mano' },
      { id: 'item-4-6', text: 'Bolsas de residuo (paquete 60x90)' },
    ],
  },
  {
    id: 'cat-5',
    title: 'Elementos de Higiene',
    items: [
      { id: 'item-5-1', text: 'Papel higiénico' },
      { id: 'item-5-2', text: 'Jabón tocador' },
      { id: 'item-5-3', text: 'Shampoo' },
      { id: 'item-5-4', text: 'Acondicionador' },
      { id: 'item-5-5', text: 'Toalla y toallas (puede ser de secado rápido)' },
      { id: 'item-5-6', text: 'Protector solar' },
      { id: 'item-5-7', text: 'Protector solar labial' },
      { id: 'item-5-8', text: 'Manteca de cacao' },
      { id: 'item-5-9', text: 'Bolsas de residuos' },
      { id: 'item-5-10', text: 'Jabón blanco para lavar la ropa' },
    ],
  },
  {
    id: 'cat-6',
    title: 'Elementos de Cocina',
    items: [
      { id: 'item-6-1', text: 'Plato hondo' },
      { id: 'item-6-2', text: 'Set de cubiertos (tenedor, cuchillo, cuchara)' },
      { id: 'item-6-3', text: '1 Vaso térmico o jarro de aluminio' },
      { id: 'item-6-4', text: '1 Abrelatas (tipo uña)' },
    ],
  },
];
