// frontend/src/components/pages/AssistancePage.test.tsx
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import AssistancePage from './AssistancePage'; // Importar usando export default
import { api } from '../../services/api';
import { UserData } from '../../types';

// Mockear el servicio api
jest.mock('../../services/api', () => ({
  api: {
    assistance: {
      getByDate: jest.fn(),
      record: jest.fn(),
    },
    // Mockear la llamada hipotética para obtener todos los usuarios/campistas
    // Si tienes un endpoint real, mockea ese.
    call: jest.fn(),
  },
}));

// Mockear useLocalStorage
jest.mock('../../hooks/useLocalStorage', () => ({
  useLocalStorage: jest.fn(),
}));

const mockCurrentUser: UserData = {
  id: 1,
  username: 'testuser',
  roles: ['USER', 'CAMPER'],
};

const mockCampersList: UserData[] = [
  { id: 1, username: 'Test User 1', roles: ['CAMPER'] },
  { id: 2, username: 'Test User 2', roles: ['CAMPER'] },
];

describe('AssistancePage', () => {
  beforeEach(() => {
    // Resetear mocks antes de cada prueba
    jest.clearAllMocks();

    // Configurar mock de useLocalStorage para devolver un usuario
    (require('../../hooks/useLocalStorage').useLocalStorage as jest.Mock).mockReturnValue({
      storedValue: mockCurrentUser,
      setValue: jest.fn(),
    });

    // Configurar mock para la llamada que obtiene todos los campistas/usuarios
    // Asumimos que api.call('/api/users') es lo que se usaría o un endpoint similar.
    // Si AssistancePage usa api.auth.getAllUsers() o algo así, mockea eso.
    (api.call as jest.Mock).mockResolvedValue({
      data: mockCampersList, // Lista de usuarios/campistas
      success: true,
    });

    // Configurar mock para getByDate (inicialmente sin registros)
    (api.assistance.getByDate as jest.Mock).mockResolvedValue({
      data: [],
      success: true,
    });

    // Configurar mock para record (simula una respuesta exitosa)
    (api.assistance.record as jest.Mock).mockImplementation(async (request) => ({
      data: {
        id: Math.floor(Math.random() * 1000), // Un ID aleatorio para el registro
        userId: request.userId,
        date: request.date,
        hasAttended: request.hasAttended,
      },
      success: true,
    }));
  });

  test('renders loading state initially', () => {
    render(<AssistancePage />);
    expect(screen.getByText(/Cargando asistencia.../i)).toBeInTheDocument();
  });

  test('renders page title and date picker after loading', async () => {
    render(<AssistancePage />);
    await waitFor(() => {
      expect(screen.getByText(/Gestión de Asistencia/i)).toBeInTheDocument();
    });
    expect(screen.getByLabelText(/Seleccionar Fecha:/i)).toBeInTheDocument();
  });

  test('fetches and displays campers for the selected date', async () => {
    (api.assistance.getByDate as jest.Mock).mockResolvedValueOnce({
      data: [
        { id: 101, userId: 1, date: '2024-07-30', hasAttended: true, userName: 'Test User 1' },
      ],
      success: true,
    });

    render(<AssistancePage />);

    await waitFor(() => {
      expect(screen.getByText('Test User 1')).toBeInTheDocument();
    });
    expect(screen.getByText('Test User 2')).toBeInTheDocument(); // User 2 no tiene registro, pero se lista

    // Test User 1 debería aparecer como presente
    const user1Button = screen.getByRole('button', { name: /Marcar Ausente/i });
    expect(user1Button).toBeInTheDocument();
    expect(user1Button.closest('li')).toHaveClass('bg-green-100');


    // Test User 2 debería aparecer como para marcar presente (o sin estado claro si es null)
    const user2Button = screen.getAllByRole('button', { name: /Marcar Presente/i }).find(btn => btn.closest('li')?.textContent?.includes('Test User 2'));
    expect(user2Button).toBeInTheDocument();
    if (user2Button) { // Chequeo para TypeScript
        expect(user2Button.closest('li')).toHaveClass('bg-gray-100'); // o bg-red-100 si hasAttended es false
    }
  });

  test('allows changing the date and re-fetches assistance data', async () => {
    render(<AssistancePage />);
    await waitFor(() => {
      expect(api.assistance.getByDate).toHaveBeenCalledTimes(1);
    });

    const dateInput = screen.getByLabelText(/Seleccionar Fecha:/i);
    fireEvent.change(dateInput, { target: { value: '2024-08-01' } });

    await waitFor(() => {
      // Debería llamarse una vez por la carga inicial, y otra por el cambio de fecha
      expect(api.assistance.getByDate).toHaveBeenCalledTimes(2);
    });
    expect(api.assistance.getByDate).toHaveBeenCalledWith('2024-08-01');
  });

  test('toggles assistance status when button is clicked', async () => {
    // Mock para que Test User 1 inicialmente no tenga registro
     (api.assistance.getByDate as jest.Mock).mockResolvedValueOnce({
      data: [], // Sin registros iniciales
      success: true,
    });

    render(<AssistancePage />);

    let presentButtonForUser1: HTMLElement | undefined;
    await waitFor(() => {
      // Esperar a que se renderice el botón "Marcar Presente" para Test User 1
      const buttons = screen.getAllByRole('button', { name: /Marcar Presente/i });
      presentButtonForUser1 = buttons.find(btn => btn.closest('li')?.textContent?.includes('Test User 1'));
      expect(presentButtonForUser1).toBeInTheDocument();
    });
     if (!presentButtonForUser1) throw new Error("Button not found");


    fireEvent.click(presentButtonForUser1);

    await waitFor(() => {
      expect(api.assistance.record).toHaveBeenCalledTimes(1);
    });
    expect(api.assistance.record).toHaveBeenCalledWith({
      userId: mockCampersList[0].id, // ID de Test User 1
      date: new Date().toISOString().split('T')[0], // Fecha por defecto
      hasAttended: true, // Se marcó como presente
    });

    // El botón debería cambiar a "Marcar Ausente"
    const absentButtonForUser1 = screen.getByRole('button', { name: /Marcar Ausente/i });
    expect(absentButtonForUser1).toBeInTheDocument();
    expect(absentButtonForUser1.closest('li')).toHaveClass('bg-green-100');

    // Hacer clic de nuevo para marcar como ausente
    fireEvent.click(absentButtonForUser1);

    await waitFor(() => {
        expect(api.assistance.record).toHaveBeenCalledTimes(2);
    });
    expect(api.assistance.record).toHaveBeenCalledWith({
        userId: mockCampersList[0].id,
        date: new Date().toISOString().split('T')[0],
        hasAttended: false, // Se marcó como ausente
    });

    // El botón debería volver a "Marcar Presente" y el fondo cambiar
    const newPresentButtonForUser1 = screen.getByRole('button', { name: /Marcar Presente/i });
    expect(newPresentButtonForUser1).toBeInTheDocument();
    expect(newPresentButtonForUser1.closest('li')).toHaveClass('bg-red-100');
  });

  test('displays error message if fetching users fails', async () => {
    (api.call as jest.Mock).mockRejectedValueOnce(new Error('Failed to fetch users'));
    render(<AssistancePage />);
    await waitFor(() => {
      expect(screen.getByText(/Error al cargar la lista de campistas./i)).toBeInTheDocument();
    });
  });

  test('displays error message if fetching assistance data fails', async () => {
    (api.assistance.getByDate as jest.Mock).mockRejectedValueOnce(new Error('Failed to fetch assistance'));
    render(<AssistancePage />);
    await waitFor(() => {
      // El error puede variar dependiendo de cómo se propague.
      // Ajusta el texto si es necesario.
      expect(screen.getByText(/Error al cargar los datos de asistencia./i)).toBeInTheDocument();
    });
  });

   test('displays message when no user is logged in', () => {
    (require('../../hooks/useLocalStorage').useLocalStorage as jest.Mock).mockReturnValue({
      storedValue: null, // No user logged in
    });
    render(<AssistancePage />);
    expect(screen.getByText(/Por favor, inicia sesión para gestionar la asistencia./i)).toBeInTheDocument();
  });

  test('displays message when no campers are available', async () => {
    (api.call as jest.Mock).mockResolvedValue({ data: [], success: true }); // No campers
    render(<AssistancePage />);
    await waitFor(() => {
      expect(screen.getByText(/No hay campistas para mostrar/i)).toBeInTheDocument();
    });
  });

});
