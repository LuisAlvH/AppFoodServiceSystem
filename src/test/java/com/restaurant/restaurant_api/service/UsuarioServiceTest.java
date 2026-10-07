package com.restaurant.restaurant_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.restaurant.restaurant_api.entity.EstadoUsuario;
import com.restaurant.restaurant_api.entity.Usuario;
import com.restaurant.restaurant_api.exception.UsuarioBloqueadoException;
import com.restaurant.restaurant_api.exception.UsuarioNoEncontradoException;
import com.restaurant.restaurant_api.repository.UsuarioRepository;

public class UsuarioServiceTest {

        @Nested
        class ObtenerUsuarioPorFirebaseUidTest {
                @Test
                void usuarioActivo() {
                        // 1. Preparar el mock
                        UsuarioRepository repository = mock(UsuarioRepository.class);

                        // 2. Crear el objeto que estamos probando
                        UsuarioService usuarioService = new UsuarioService(repository);

                        // 3. Preparar los datos
                        Usuario usuario = new Usuario("ID_prueba", "nombre_prueba");
                        usuario.setEstado(EstadoUsuario.ACTIVO);
                        // 4. Definir el comportamiento del mock
                        when(repository.findByFirebaseUid("ID_prueba"))
                                        .thenReturn(Optional.of(usuario));

                        // 5. Ejecutar el método REAL que quiero probar
                        Usuario resultado = usuarioService.obtenerUsuarioPorFirebaseUid("ID_prueba");

                        // 6. Comprobar el resultado
                        assertEquals(usuario, resultado);
                        // 7. Comprobar que el Service llamó al Repository
                        verify(repository).findByFirebaseUid("ID_prueba");
                        // REPORTE
                        System.out.println("""

                                        ========================================
                                        TEST: ObtenerUsuarioPorFirebaseUidTest - usuarioActivo
                                        ========================================
                                        [OK] RESULTADO: PASO
                                        UID enviado: ID_prueba
                                        UID obtenido: %s
                                        Email obtenido: %s
                                        Estado : %s
                                        ========================================
                                        """.formatted(
                                        resultado.getFirebaseUid(),
                                        resultado.getEmail(),
                                        resultado.getEstado()));

                }

                @Test
                void usuarioNoEncontrado() {
                        UsuarioRepository repository = mock(UsuarioRepository.class);
                        UsuarioService usuarioService = new UsuarioService(repository);

                        when(repository.findByFirebaseUid("UID_inexistente"))
                                        .thenReturn(Optional.empty());

                        assertThrows(
                                        UsuarioNoEncontradoException.class,
                                        () -> usuarioService.obtenerUsuarioPorFirebaseUid("UID_inexistente"));

                        verify(repository).findByFirebaseUid("UID_inexistente");

                        System.out.println("""

                                        ========================================
                                        TEST: ObtenerUsuarioPorFirebaseUidTest - usuarioNoEncontrado
                                        ========================================
                                        [OK] RESULTADO: PASO

                                        UID enviado   : UID_inexistente
                                        Email         : Inexistente
                                        Estado        : Inexistente
                                        Excepcion     : UsuarioNoEncontradoException

                                        ========================================
                                        """);

                }

                @Test
                void usuarioBloqueado() {
                        UsuarioRepository repository = mock(UsuarioRepository.class);
                        UsuarioService usuarioService = new UsuarioService(repository);
                        Usuario usuario = new Usuario("ID_bloqueado", "nombre_prueba_bloqueado");
                        usuario.setEstado(EstadoUsuario.BLOQUEADO);

                        when(repository.findByFirebaseUid("ID_bloqueado"))
                                        .thenReturn(Optional.of(usuario));

                        assertThrows(UsuarioBloqueadoException.class, () -> {
                                usuarioService.obtenerUsuarioPorFirebaseUid("ID_bloqueado");
                        });
                        verify(repository).findByFirebaseUid("ID_bloqueado");
                        System.out.println("""

                                        ========================================
                                        TEST: ObtenerUsuarioPorFirebaseUidTest - usuarioBloqueado
                                        ========================================
                                        [OK] RESULTADO: PASO

                                        UID enviado   : %s
                                        Email         : %s
                                        Estado        : %s
                                        Excepcion     : UsuarioBloqueadoException

                                        ========================================
                                        """.formatted(
                                        usuario.getFirebaseUid(),
                                        usuario.getEmail(),
                                        usuario.getEstado()));
                }

        }

        @Nested
        class ObtenerOCrearUsuarioTest {
                @Test
                void usuarioInexistente_CrearUsuario() {
                        UsuarioRepository repository = mock(UsuarioRepository.class);
                        UsuarioService usuarioService = new UsuarioService(repository);

                        String uid = "UID_inexistente";
                        String email = "nuevo@gmail.com";

                        when(repository.findByFirebaseUid("UID_inexistente"))
                                        .thenReturn(Optional.empty());
                        Usuario usuarioGuardado = new Usuario(uid, email);
                        usuarioGuardado.setEstado(EstadoUsuario.ACTIVO);
                        when(repository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

                        ResultadoSync resultado = usuarioService.obtenerOCrearUsuario(uid, email);
                        assertTrue(resultado.creado());
                        assertEquals(usuarioGuardado, resultado.usuario());
                        verify(repository).findByFirebaseUid(uid);
                        verify(repository).save(any(Usuario.class));

                        System.out.println("""

                                        ========================================
                                        TEST: ObtenerOCrearUsuarioTest - usuarioInexistente_creaUsuario
                                        ========================================
                                        [OK] RESULTADO: PASO

                                        UID enviado   : %s
                                        Email         : %s
                                        Creado        : %s
                                        Estado        : %s


                                        ========================================
                                        """.formatted(
                                        uid,
                                        email,
                                        resultado.creado(),
                                        resultado.usuario().getEstado()));

                }

                @Test
                void usuarioExistente_ActualizarAcceso() {
                        UsuarioRepository repository = mock(UsuarioRepository.class); /// <--Creame un objeto falso que
                                                                                      /// se comporte como un
                                                                                      /// UsuarioRepository,
                        // mando todos sus metodos y propiedades, pero que no haga nada real, solo
                        // simule el comportamiento de un UsuarioRepository.

                        UsuarioService usuarioService = new UsuarioService(repository); /// <-- le pasamos los datos
                                                                                        /// falsos al usuarioService

                        String uid = "UID_existente";
                        String email = "existente@gmail.com";

                        Usuario usuarioExistente = new Usuario(uid, email);
                        usuarioExistente.setEstado(EstadoUsuario.ACTIVO);// <--Este objeto es el que vamos a simular que
                                                                         // ya existe en la base de datos,
                        // ue vamos a actualizar su ultimo acceso.

                        when(repository.findByFirebaseUid(uid)).thenReturn(Optional.of(usuarioExistente));// "Cuando el
                                                                                                          // servicio
                                                                                                          // busque
                                                                                                          // UID_existente,
                                                                                                          // hacé de
                                                                                                          // cuenta que
                                                                                                          // el
                                                                                                          // repositorio
                                                                                                          // encontró
                                                                                                          // usuarioExistente.
                        when(repository.save(any(Usuario.class))).thenReturn(usuarioExistente);
                        // Cuando el servicio guarde cualquier Usuario, simulá que el repositorio
                        // devuelve usuarioExistente

                        /// ani() es un matcher que significa "cualquier objeto de tipo Usuario", no
                        /// importa cual sea, solo que sea de tipo Usuario.

                        /* Comienza el test */
                        ResultadoSync resultado = usuarioService.obtenerOCrearUsuario(uid, email);
                        assertFalse(resultado.creado());
                        assertNotNull(resultado.usuario().getUltimoAcceso());

                        // verify comprobamos las llamadas a los métodos del mock, para asegurarnos que
                        // se llamaron correctamente.
                        verify(repository).findByFirebaseUid(uid);
                        /* Comprobamos que se haya llamado al método del repositorio */
                        verify(repository).save(usuarioExistente);
                        /* Comprobamos que se haya llamado al método save del repositorio */
                        System.out.println("""

                                        ========================================
                                        TEST: ObtenerOCrearUsuarioTest - usuarioExistente_actualizaUltimoAcceso
                                        ========================================
                                        [OK] RESULTADO: PASO

                                        UID enviado     : %s
                                        Email           : %s
                                        Creado          : %s
                                        Ultimo acceso   : %s

                                        ========================================
                                        """.formatted(
                                        uid,
                                        email,
                                        resultado.creado(),
                                        resultado.usuario().getUltimoAcceso()));

                }

                @Test
                void emailDiferente_actualizaEmail() {
                        UsuarioRepository repository = mock(UsuarioRepository.class);
                        UsuarioService usuarioService = new UsuarioService(repository);

                        String uid = "UID_existente";
                        String emailActual = "viejo@hotmail.com";
                        String emailNuevo = "nuevo@hotmail.com";
                        Usuario usuario = new Usuario(uid, emailActual);
                        usuario.setEstado(EstadoUsuario.ACTIVO);

                        when(repository.findByFirebaseUid(uid)).thenReturn(Optional.of(usuario));

                        when(repository.save(any(Usuario.class))).thenReturn(usuario);

                        ResultadoSync resultado = usuarioService.obtenerOCrearUsuario(uid, emailNuevo);
                        assertFalse(resultado.creado());
                        assertEquals(emailNuevo, resultado.usuario().getEmail());

                        verify(repository).findByFirebaseUid(uid);
                        verify(repository).save(usuario);

                        System.out.println("""

                                        ========================================
                                        TEST: ObtenerOCrearUsuarioTest - emailDiferente_actualizaEmail
                                        ========================================
                                        [OK] RESULTADO: PASO

                                        UID          : %s
                                        Email anterior: %s
                                        Email nuevo   : %s
                                        Creado        : %s

                                        ========================================
                                        """.formatted(
                                        uid,
                                        emailActual,
                                        resultado.usuario().getEmail(),
                                        resultado.creado()));
                }

                @Test
                void emailNull_noActualizaEmail() {
                        UsuarioRepository repository = mock(UsuarioRepository.class);
                        UsuarioService usuarioService = new UsuarioService(repository);

                        String uid = "ID_email_null";
                        String emailActual = "actual@gmail.com";

                        Usuario usuario = new Usuario(uid, emailActual);
                        usuario.setEstado(EstadoUsuario.ACTIVO);

                        when(repository.findByFirebaseUid(uid))
                                        .thenReturn(Optional.of(usuario));

                        when(repository.save(any(Usuario.class)))
                                        .thenReturn(usuario);

                        ResultadoSync resultado = usuarioService.obtenerOCrearUsuario(uid, null);

                        assertFalse(resultado.creado());
                        assertEquals(emailActual, resultado.usuario().getEmail());

                        verify(repository).findByFirebaseUid(uid);
                        verify(repository).save(usuario);

                        System.out.println("""

                                        ========================================
                                        TEST: ObtenerOCrearUsuarioTest - emailNull_noActualizaEmail
                                        ========================================
                                        [OK] RESULTADO: PASO

                                        UID enviado : %s
                                        Email final  : %s
                                        Creado       : %s

                                        ========================================
                                        """.formatted(
                                        uid,
                                        resultado.usuario().getEmail(),
                                        resultado.creado()));
                }

                @Test
                void usuarioExistente_emailIgual_noActualizaEmail() {

                        UsuarioRepository repository = mock(UsuarioRepository.class);
                        UsuarioService usuarioService = new UsuarioService(repository);

                        String uid = "ID_email_igual";
                        String email = "igual@gmail.com";

                        Usuario usuario = new Usuario(uid, email);
                        usuario.setEstado(EstadoUsuario.ACTIVO);

                        when(repository.findByFirebaseUid(uid))
                                        .thenReturn(Optional.of(usuario));

                        when(repository.save(any(Usuario.class)))
                                        .thenReturn(usuario);

                        ResultadoSync resultado = usuarioService.obtenerOCrearUsuario(uid, email);

                        assertFalse(resultado.creado());
                        assertEquals(email, resultado.usuario().getEmail());

                        verify(repository).findByFirebaseUid(uid);
                        verify(repository).save(usuario);

                        System.out.println("""

                                        ========================================
                                        TEST: ObtenerOCrearUsuarioTest - usuarioExistente_emailIgual_noActualizaEmail
                                        ========================================
                                        [OK] RESULTADO: PASO

                                        UID       : %s
                                        Email     : %s
                                        Creado    : %s
                                        Resultado : Email sin cambios

                                        ========================================
                                        """.formatted(
                                        uid,
                                        resultado.usuario().getEmail(),
                                        resultado.creado()));
                }

                @Test
                void usuarioBloqueado_lanzaExcepcion() {

                        UsuarioRepository repository = mock(UsuarioRepository.class);
                        UsuarioService usuarioService = new UsuarioService(repository);

                        String uid = "ID_bloqueado";
                        String email = "bloqueado@gmail.com";

                        Usuario usuario = new Usuario(uid, email);
                        usuario.setEstado(EstadoUsuario.BLOQUEADO);

                        when(repository.findByFirebaseUid(uid))
                                        .thenReturn(Optional.of(usuario));

                        assertThrows(UsuarioBloqueadoException.class,
                                        () -> usuarioService.obtenerOCrearUsuario(uid, email));

                        verify(repository).findByFirebaseUid(uid);
                        verify(repository, never()).save(any(Usuario.class));

                        System.out.println("""

                                        ========================================
                                        TEST: ObtenerOCrearUsuarioTest - usuarioBloqueado_lanzaExcepcion
                                        ========================================
                                        [OK] RESULTADO: PASO

                                        UID       : %s
                                        Email     : %s
                                        Estado    : %s
                                        Excepcion : UsuarioBloqueadoException

                                        ========================================
                                        """.formatted(
                                        usuario.getFirebaseUid(),
                                        usuario.getEmail(),
                                        usuario.getEstado()));
                }

                @Test
                void errorIntegridad_usuarioApareceDespues_devuelveExistente() {

                        UsuarioRepository repository = mock(UsuarioRepository.class);
                        UsuarioService usuarioService = new UsuarioService(repository);

                        String uid = "ID_carrera";
                        String email = "carrera@gmail.com";

                        Usuario usuarioExistente = new Usuario(uid, email);
                        usuarioExistente.setEstado(EstadoUsuario.ACTIVO);

                        when(repository.findByFirebaseUid(uid)).thenReturn(Optional.empty(),
                                        Optional.of(usuarioExistente));

                        when(repository.save(any(Usuario.class)))
                                        .thenThrow(new DataIntegrityViolationException("UID duplicado"))
                                        .thenReturn(usuarioExistente);

                        ResultadoSync resultado = usuarioService.obtenerOCrearUsuario(uid, email);

                        assertFalse(resultado.creado());
                        assertEquals(usuarioExistente, resultado.usuario());

                        verify(repository, times(2))
                                        .findByFirebaseUid(uid);

                        verify(repository, times(2)).save(any(Usuario.class));

                        System.out.println("""

                                        ========================================
                                        TEST: ObtenerOCrearUsuarioTest - errorIntegridad_usuarioAparece
                                        ========================================
                                        [OK] RESULTADO: PASO

                                        UID       : %s
                                        Creado    : %s
                                        Resultado : Usuario existente recuperado

                                        ========================================
                                        """.formatted(
                                        uid,
                                        resultado.creado(),
                                        resultado.usuario().getFirebaseUid()));
                }

                @Test
                void errorIntegridad_usuarioNoAparece_relanzaExcepcion() {

                        UsuarioRepository repository = mock(UsuarioRepository.class);
                        UsuarioService usuarioService = new UsuarioService(repository);

                        String uid = "ID_error";
                        String email = "error@gmail.com";

                        when(repository.findByFirebaseUid(uid))
                                        .thenReturn(
                                                        Optional.empty(),
                                                        Optional.empty());

                        DataIntegrityViolationException excepcion = new DataIntegrityViolationException(
                                        "Error de integridad");

                        when(repository.save(any(Usuario.class)))
                                        .thenThrow(excepcion);

                        assertThrows(DataIntegrityViolationException.class,
                                        () -> usuarioService.obtenerOCrearUsuario(uid, email));

                        verify(repository, times(2))
                                        .findByFirebaseUid(uid);

                        verify(repository).save(any(Usuario.class));

                        System.out.println("""

                                        ========================================
                                        TEST: ObtenerOCrearUsuarioTest - errorIntegridad_usuarioNoAparece
                                        ========================================
                                        [OK] RESULTADO: PASO

                                        UID       : %s
                                        Excepcion : DataIntegrityViolationException
                                        Resultado : Excepcion relanzada

                                        ========================================
                                        """.formatted(
                                        uid,
                                        excepcion.getClass().getSimpleName()));
                }

        }

}
