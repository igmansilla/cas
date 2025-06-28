// frontend/src/components/pages/AssistancePage.test.tsx
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import AssistancePage from './AssistancePage';
import { api } from '../../services/api';
import type { UserData, UserDto, AssistanceRecord } from '../../types';

// Mockear el servicio api completo
jest.mock('../../services/api', () => ({
  api: {
    assistance: {
      getByDate: jest.fn(),
      record: jest.fn(),
      getForSupervisedCampers: jest.fn(), // Nuevo mock
    },
    supervision: {
      getSupervisedCampers: jest.fn(), // Nuevo mock
      getAllAcampantes: jest.fn(),   // Nuevo mock
    },
    call: jest.fn(), // Mantener por si alguna prueba antigua lo usa indirectamente
  },
}));

// Mockear useLocalStorage
jest.mock('../../hooks/useLocalStorage', () => ({
  useLocalStorage: jest.fn(),
}));

const mockUserDirigente: UserData = {
  id: 1,
  username: 'dirigenteUser',
  roles: ['ROLE_USER', 'ROLE_DIRIGENTE'],
};

const mockUserAdmin: UserData = {
  id: 2,
  username: 'adminUser',
  roles: ['ROLE_USER', 'ROLE_ADMIN'],
};

const mockUserStaff: UserData = {
  id: 3,
  username: 'staffUser',
  roles: ['ROLE_USER', 'ROLE_STAFF'],
};

const mockUserSimple: UserData = {
  id: 4,
  username: 'simpleUser',
  roles: ['ROLE_USER'],
};

const mockAcampantesList: UserDto[] = [
  { id: 101, username: 'Acampante Supervisado 1', roles: ['ROLE_ACAMPANTE'] },
  { id: 102, username: 'Acampante Supervisado 2', roles: ['ROLE_ACAMPANTE'] },
];

const mockAllAcampantesList: UserDto[] = [
  ...mockAcampantesList,
  { id: 103, username: 'Otro Acampante 3', roles: ['ROLE_ACAMPANTE'] },
];

const today = new Date().toISOString().split('T')[0];

describe('AssistancePage', () => {
  const setupMocks = (currentUser: UserData | null) => {
    (require('../../hooks/useLocalStorage').useLocalStorage as jest.Mock).mockReturnValue({
      storedValue: currentUser,
      setValue: jest.fn(),
    });

    // Resetear todos los mocks de api para cada configuración
    (api.supervision.getSupervisedCampers as jest.Mock).mockReset();
    (api.supervision.getAllAcampantes as jest.Mock).mockReset();
    (api.assistance.getForSupervisedCampers as jest.Mock).mockReset();
    (api.assistance.getByDate as jest.Mock).mockReset();
    (api.assistance.record as jest.Mock).mockReset();

    // Configuración por defecto de los mocks (pueden ser sobreescritos por prueba específica)
    (api.supervision.getSupervisedCampers as jest.Mock).mockResolvedValue({ data: mockAcampantesList, success: true });
    (api.supervision.getAllAcampantes as jest.Mock).mockResolvedValue({ data: mockAllAcampantesList, success: true });
    (api.assistance.getForSupervisedCampers as jest.Mock).mockResolvedValue({ data: [], success: true });
    (api.assistance.getByDate as jest.Mock).mockResolvedValue({ data: [], success: true });
    (api.assistance.record as jest.Mock).mockImplementation(async (request: { userId: number; date: string; hasAttended: boolean }) => ({
      data: { id: Math.random(), userId: request.userId, date: request.date, hasAttended: request.hasAttended },
      success: true,
    }));
  };

  test('renders login message if no user is logged in', () => {
    setupMocks(null);
    render(<AssistancePage />);
    expect(screen.getByText(/Por favor, inicia sesión para gestionar la asistencia./i)).toBeInTheDocument();
  });

  test('renders no permission message for simple user', () => {
    setupMocks(mockUserSimple);
    render(<AssistancePage />);
    expect(screen.getByText(/No tienes permisos para gestionar la asistencia./i)).toBeInTheDocument();
  });

  describe('when logged in as DIRIGENTE', () => {
    beforeEach(() => {
      setupMocks(mockUserDirigente);
    });

    test('fetches and displays supervised campers and their assistance', async () => {
      const assistanceForSupervised: AssistanceRecord[] = [
        { id: 1, userId: 101, date: today, hasAttended: true, userName: 'Acampante Supervisado 1' },
      ];
      (api.assistance.getForSupervisedCampers as jest.Mock).mockResolvedValueOnce({ data: assistanceForSupervised, success: true });

      render(<AssistancePage />);

      await waitFor(() => {
        expect(api.supervision.getSupervisedCampers).toHaveBeenCalledWith(mockUserDirigente.id);
      });
      await waitFor(() => {
        expect(api.assistance.getForSupervisedCampers).toHaveBeenCalledWith(mockUserDirigente.id, today);
      });

      expect(screen.getByText('Acampante Supervisado 1')).toBeInTheDocument();
      expect(screen.getByText('Acampante Supervisado 2')).toBeInTheDocument();
      // Acampante 1 está presente
      const acampante1Button = screen.getByRole('button', { name: /Marcar Ausente/i });
      expect(acampante1Button.closest('li')?.textContent).toContain('Acampante Supervisado 1');
      // Acampante 2 no tiene registro, debería estar para marcar presente
      const acampante2Button = screen.getAllByRole('button', { name: /Marcar Presente/i }).find(btn => btn.closest('li')?.textContent?.includes('Acampante Supervisado 2'));
      expect(acampante2Button).toBeInTheDocument();
    });

    test('toggles assistance for a supervised camper', async () => {
        render(<AssistancePage />);
        await waitFor(() => expect(screen.getByText('Acampante Supervisado 1')).toBeInTheDocument());

        const presentButton = screen.getAllByRole('button', { name: /Marcar Presente/i }).find(btn => btn.closest('li')?.textContent?.includes('Acampante Supervisado 1'))!;
        fireEvent.click(presentButton);

        await waitFor(() => expect(api.assistance.record).toHaveBeenCalledWith({
          userId: 101, date: today, hasAttended: true,
        }));
        expect(screen.getByRole('button', { name: /Marcar Ausente/i })).toBeInTheDocument();
      });
  });

  describe('when logged in as ADMIN', () => {
    beforeEach(() => {
      setupMocks(mockUserAdmin);
    });

    test('fetches and displays all acampantes and their assistance', async () => {
      const assistanceForAll: AssistanceRecord[] = [
        { id: 1, userId: 101, date: today, hasAttended: false, userName: 'Acampante Supervisado 1' },
        { id: 3, userId: 103, date: today, hasAttended: true, userName: 'Otro Acampante 3'},
      ];
      (api.assistance.getByDate as jest.Mock).mockResolvedValueOnce({ data: assistanceForAll, success: true });

      render(<AssistancePage />);

      await waitFor(() => {
        expect(api.supervision.getAllAcampantes).toHaveBeenCalled();
      });
      await waitFor(() => {
        expect(api.assistance.getByDate).toHaveBeenCalledWith(today);
      });

      expect(screen.getByText('Acampante Supervisado 1')).toBeInTheDocument();
      expect(screen.getByText('Otro Acampante 3')).toBeInTheDocument();

      const acampante1Button = screen.getAllByRole('button', { name: /Marcar Presente/i }).find(btn => btn.closest('li')?.textContent?.includes('Acampante Supervisado 1'));
      expect(acampante1Button).toBeInTheDocument();

      const acampante3Button = screen.getAllByRole('button', { name: /Marcar Ausente/i }).find(btn => btn.closest('li')?.textContent?.includes('Otro Acampante 3'));
      expect(acampante3Button).toBeInTheDocument();
    });
  });

  describe('when logged in as STAFF', () => {
    beforeEach(() => {
      setupMocks(mockUserStaff);
    });

    test('fetches and displays all acampantes (similar to ADMIN)', async () => {
      (api.assistance.getByDate as jest.Mock).mockResolvedValueOnce({ data: [], success: true }); // No specific assistance for this test

      render(<AssistancePage />);

      await waitFor(() => {
        expect(api.supervision.getAllAcampantes).toHaveBeenCalled();
      });
      await waitFor(() => {
        expect(api.assistance.getByDate).toHaveBeenCalledWith(today);
      });
      expect(screen.getByText('Acampante Supervisado 1')).toBeInTheDocument();
      expect(screen.getByText('Otro Acampante 3')).toBeInTheDocument();
    });
  });


  test('allows changing date and re-fetches data accordingly (DIRIGENTE)', async () => {
    setupMocks(mockUserDirigente);
    render(<AssistancePage />);

    await waitFor(() => expect(api.assistance.getForSupervisedCampers).toHaveBeenCalledWith(mockUserDirigente.id, today));

    const newDate = '2024-08-15';
    const dateInput = screen.getByLabelText(/Seleccionar Fecha:/i);
    fireEvent.change(dateInput, { target: { value: newDate } });

    await waitFor(() => expect(api.assistance.getForSupervisedCampers).toHaveBeenCalledWith(mockUserDirigente.id, newDate));
    expect(api.assistance.getForSupervisedCampers).toHaveBeenCalledTimes(2); // Initial + change
  });

  test('displays error message if fetching campers list fails (DIRIGENTE)', async () => {
    setupMocks(mockUserDirigente);
    (api.supervision.getSupervisedCampers as jest.Mock).mockRejectedValueOnce(new Error('Failed to fetch supervised campers'));
    render(<AssistancePage />);
    await waitFor(() => {
      expect(screen.getByText(/Error: Failed to fetch supervised campers/i)).toBeInTheDocument();
    });
  });

  test('displays error message if fetching assistance data fails (DIRIGENTE)', async () => {
    setupMocks(mockUserDirigente);
    (api.assistance.getForSupervisedCampers as jest.Mock).mockRejectedValueOnce(new Error('Failed to fetch assistance for supervised'));
    render(<AssistancePage />);
    await waitFor(() => {
      // The error message might be generic "Error al cargar los datos de asistencia."
      // or the specific one if not caught and re-thrown
      expect(screen.getByText(/Error: Failed to fetch assistance for supervised/i)).toBeInTheDocument();
    });
  });

  test('displays message when DIRIGENTE has no supervised campers', async () => {
    setupMocks(mockUserDirigente);
    (api.supervision.getSupervisedCampers as jest.Mock).mockResolvedValue({ data: [], success: true });
    render(<AssistancePage />);
    await waitFor(() => {
      expect(screen.getByText(/No tienes acampantes supervisados para mostrar./i)).toBeInTheDocument();
    });
  });

  test('displays message when ADMIN/STAFF find no acampantes', async () => {
    setupMocks(mockUserAdmin);
    (api.supervision.getAllAcampantes as jest.Mock).mockResolvedValue({ data: [], success: true });
    render(<AssistancePage />);
    await waitFor(() => {
      expect(screen.getByText(/No hay acampantes para mostrar./i)).toBeInTheDocument();
    });
  });

});
