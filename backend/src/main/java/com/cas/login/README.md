# Login Module

This module handles user authentication and authorization for the application. It uses Spring Security to manage roles and permissions, ensuring that users can only access the resources appropriate for their roles.

Key functionalities include:
- User login with username and password.
- Role-based access control for different parts of the API.
- Initialization of default user accounts for administrative and standard operational roles.

For more detailed information on specific roles, permissions, and initial user setup, please see the sections below.

## Roles

The system defines the following roles to manage user access:

*   **`ROLE_ADMIN`**:
    *   **Description**: Users with this role have full administrative privileges over the system.
    *   **Capabilities**: Can manage all user accounts (Dirigentes, other Admins), configure system settings, and access all data including acampante and dirigente information.
    *   **Typical Use Cases**: System setup, user management, troubleshooting, accessing sensitive or comprehensive data views.

*   **`ROLE_DIRIGENTE`**:
    *   **Description**: This role is intended for camp leaders or organizers (Dirigentes).
    *   **Capabilities**: Can manage information related to `acampantes` (campers), view their own profile information. They cannot manage other `Dirigente` accounts or system-wide settings unless they also have `ROLE_ADMIN`.
    *   **Typical Use Cases**: Registering and updating camper details, organizing camper groups, viewing camp logistics relevant to their role.

*   **`ROLE_ACAMPANTE`**:
    *   **Status**: Currently, `ROLE_ACAMPANTE` is defined in the system but is not used for active user logins. Acampantes are managed as data by users with `ROLE_DIRIGENTE` or `ROLE_ADMIN` rather than being users who log into the system themselves.

## Permissions and Endpoint Protection

Authentication is primarily handled via form-based login. Once authenticated, user access to API endpoints is restricted based on their assigned roles. The `SecurityConfig.java` class defines these protections.

Key protected API endpoint groups include:

*   **`/api/acampantes/**`**:
    *   Accessible by: `ROLE_DIRIGENTE`, `ROLE_ADMIN`
    *   Purpose: Allows management of camper information (creating, reading, updating, deleting).

*   **`/api/dirigentes/**`**:
    *   Accessible by: `ROLE_ADMIN`
    *   Purpose: Allows management of Dirigente user accounts and their details.

*   **`/api/admin/**`**:
    *   Accessible by: `ROLE_ADMIN`
    *   Purpose: Provides access to administrative functions and system-wide configurations.

*   **`/api/user/me`**:
    *   Accessible by: Any authenticated user.
    *   Purpose: Allows users to retrieve their own profile information.

Other paths like `/login`, static resources (`/css/**`, `/js/**`), and error pages are configured to be publicly accessible.

## User Initialization

Upon application startup, the `DataInitializer.java` class ensures that a set of default users and roles are present in the system. This is particularly useful for development and initial deployment.

The default users created are:

*   **Username**: `admin`
    *   **Password**: `adminpass`
    *   **Roles**: `ROLE_ADMIN`

*   **Username**: `dirigente1`
    *   **Password**: `dirigentepass`
    *   **Roles**: `ROLE_DIRIGENTE`

*   **Username**: `superdirigente`
    *   **Password**: `superpass`
    *   **Roles**: `ROLE_DIRIGENTE`, `ROLE_ADMIN`

These users can be used to log in and test the application's functionality according to their assigned roles. For production environments, it is strongly recommended to change these default passwords.
