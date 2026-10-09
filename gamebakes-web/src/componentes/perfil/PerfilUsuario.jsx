import React, { useState, useEffect } from 'react';
import { useAxios } from '../autenticacion/useAxios.js';

export default function PerfilUsuario({ usuarioId, rol }) {
    const api = useAxios();
    const [perfil, setPerfil] = useState(null);
    const [modoEdicion, setModoEdicion] = useState(false);
    const [form, setForm] = useState({
        nombreCompleto: '', telefono: '', direccion: ''
    });
    const [cargando, setCargando] = useState(true);
    const [guardando, setGuardando] = useState(false);
    const [mensaje, setMensaje] = useState({ tipo: '', texto: '' });

    const colorTema = rol === 'vendedor' ? '#9b59b6' : '#00d4ff';

    useEffect(() => {
        if (usuarioId) cargarPerfil();
    }, [usuarioId]);

    const cargarPerfil = async () => {
        setCargando(true);
        setMensaje({ tipo: '', texto: '' });

        try {
            // useAxios inyecta el token automáticamente
            const response = await api.get(`/api/perfil/usuario/${usuarioId}`);
            const data = response.data;

            setPerfil(data);
            setForm({
                nombreCompleto: data.nombreCompleto || '',
                telefono: data.telefono || '',
                direccion: data.direccion || ''
            });
        } catch (error) {
            if (error.response?.status === 404) {
                await crearPerfilAutomatico();
            } else {
                console.error('Error al cargar perfil:', error);
                setMensaje({ tipo: 'error', texto: 'Error de conexión al servidor' });
            }
        } finally {
            setCargando(false);
        }
    };

    const crearPerfilAutomatico = async () => {
        try {
            const nuevoPerfil = {
                usuarioId: usuarioId,
                nombreCompleto: 'Usuario Nuevo',
                telefono: '',
                direccion: ''
            };

            const response = await api.post(`/api/perfil`, nuevoPerfil);
            const data = response.data;

            setPerfil(data);
            setForm({
                nombreCompleto: data.nombreCompleto || '',
                telefono: data.telefono || '',
                direccion: data.direccion || ''
            });
            setMensaje({ tipo: 'exito', texto: 'Perfil creado automáticamente' });
        } catch (error) {
            if (error.response?.status === 409) {
                await cargarPerfil();
            } else {
                console.error('Error al crear perfil:', error);
                setMensaje({ tipo: 'error', texto: 'Error de conexión al crear perfil' });
            }
        }
    };

    const handleGuardar = async (e) => {
        e.preventDefault();
        setGuardando(true);
        setMensaje({ tipo: '', texto: '' });

        if (form.nombreCompleto.trim().length < 2) {
            setMensaje({ tipo: 'error', texto: 'El nombre completo debe tener al menos 2 caracteres' });
            setGuardando(false);
            return;
        }

        if (form.telefono && !/^\d+$/.test(form.telefono.replace(/\s/g, ''))) {
            setMensaje({ tipo: 'error', texto: 'El teléfono solo debe contener dígitos' });
            setGuardando(false);
            return;
        }

        try {
            const perfilActualizado = {
                nombreCompleto: form.nombreCompleto,
                telefono: form.telefono,
                direccion: form.direccion
            };

            const response = await api.put(`/api/perfil/usuario/${usuarioId}`, perfilActualizado);
            const data = response.data;

            setPerfil(data);
            setModoEdicion(false);
            setMensaje({ tipo: 'exito', texto: 'Perfil actualizado correctamente' });
        } catch (error) {
            console.error('Error al guardar perfil:', error);
            setMensaje({ tipo: 'error', texto: error.response?.data?.message || 'Error al actualizar el perfil' });
        } finally {
            setGuardando(false);
        }
    };

    const handleCancelar = () => {
        setModoEdicion(false);
        if (perfil) {
            setForm({
                nombreCompleto: perfil.nombreCompleto || '',
                telefono: perfil.telefono || '',
                direccion: perfil.direccion || ''
            });
        }
        setMensaje({ tipo: '', texto: '' });
    };

    if (cargando) {
        return (
            <div style={{ textAlign: 'center', padding: '60px' }}>
                <div style={{ fontSize: '3rem', marginBottom: '20px' }}>⏳</div>
                <p style={{ color: '#888', fontSize: '1.2rem' }}>Cargando perfil...</p>
            </div>
        );
    }

    const inputStyle = { width: '100%', padding: '14px', fontSize: '0.95rem', backgroundColor: 'rgba(0,0,0,0.4)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '12px', color: 'white', outline: 'none', boxSizing: 'border-box', transition: '0.3s' };
    const inputReadOnlyStyle = { ...inputStyle, backgroundColor: 'rgba(0,0,0,0.2)', cursor: 'not-allowed', opacity: '0.7' };
    const labelStyle = { color: colorTema, display: 'block', marginBottom: '8px', fontSize: '0.85rem', fontWeight: 'bold', letterSpacing: '1px' };
    const inputGroup = { marginBottom: '20px' };

    return (
        <div style={{ animation: 'fadeIn 0.5s' }}>
            <div style={{ background: `linear-gradient(135deg, rgba(0,0,0,0.8), ${colorTema}33)`, padding: '40px', borderRadius: '20px', border: `1px solid ${colorTema}`, marginBottom: '30px', boxShadow: `0 10px 30px ${colorTema}22` }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                    <div>
                        <h1 style={{ color: colorTema, margin: 0, fontSize: '2.5rem', textTransform: 'uppercase', letterSpacing: '2px' }}>👤 MI PERFIL</h1>
                        <p style={{ color: '#888', marginTop: '10px', fontSize: '1rem' }}>{modoEdicion ? 'Edita tu información personal' : 'Visualiza y gestiona tu perfil'}</p>
                    </div>
                    {!modoEdicion && (
                        <button onClick={() => setModoEdicion(true)} style={{ padding: '12px 24px', backgroundColor: colorTema, color: 'black', border: 'none', borderRadius: '10px', fontWeight: 'bold', cursor: 'pointer', textTransform: 'uppercase', fontSize: '0.9rem', transition: '0.3s' }}>
                            ✏️ EDITAR
                        </button>
                    )}
                </div>

                {mensaje.texto && (
                    <div style={{ padding: '15px', borderRadius: '10px', marginBottom: '20px', backgroundColor: mensaje.tipo === 'exito' ? 'rgba(0, 212, 255, 0.1)' : 'rgba(255, 77, 77, 0.1)', border: `1px solid ${mensaje.tipo === 'exito' ? '#00d4ff' : '#ff4d4d'}`, color: mensaje.tipo === 'exito' ? '#00d4ff' : '#ff4d4d', textAlign: 'center', fontWeight: 'bold' }}>
                        {mensaje.tipo === 'exito' ? '✅' : '❌'} {mensaje.texto}
                    </div>
                )}

                <form onSubmit={handleGuardar}>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '20px' }}>
                        <div style={inputGroup}><label style={labelStyle}>ROL</label><input type="text" style={inputReadOnlyStyle} value={rol.toUpperCase()} readOnly /></div>
                        <div style={inputGroup}><label style={labelStyle}>NOMBRE COMPLETO *</label><input type="text" style={modoEdicion ? inputStyle : inputReadOnlyStyle} value={form.nombreCompleto} onChange={(e) => setForm({ ...form, nombreCompleto: e.target.value })} readOnly={!modoEdicion} placeholder="Tu nombre completo" /></div>
                        <div style={inputGroup}><label style={labelStyle}>TELÉFONO (OPCIONAL)</label><input type="text" style={modoEdicion ? inputStyle : inputReadOnlyStyle} value={form.telefono} onChange={(e) => setForm({ ...form, telefono: e.target.value })} readOnly={!modoEdicion} placeholder="Solo dígitos" /></div>
                        <div style={inputGroup}><label style={labelStyle}>DIRECCIÓN (OPCIONAL)</label><input type="text" style={modoEdicion ? inputStyle : inputReadOnlyStyle} value={form.direccion} onChange={(e) => setForm({ ...form, direccion: e.target.value })} readOnly={!modoEdicion} placeholder="Tu dirección física" /></div>
                    </div>

                    {modoEdicion && (
                        <div style={{ display: 'flex', gap: '15px', marginTop: '30px' }}>
                            <button type="submit" disabled={guardando} style={{ flex: 1, padding: '16px', backgroundColor: colorTema, color: 'black', border: 'none', borderRadius: '12px', fontWeight: 'bold', cursor: guardando ? 'not-allowed' : 'pointer', textTransform: 'uppercase', fontSize: '1rem', opacity: guardando ? 0.6 : 1, boxShadow: `0 5px 15px ${colorTema}4d` }}>
                                {guardando ? '💾 GUARDANDO...' : '💾 GUARDAR CAMBIOS'}
                            </button>
                            <button type="button" onClick={handleCancelar} disabled={guardando} style={{ flex: 1, padding: '16px', backgroundColor: 'transparent', color: '#ff4d4d', border: '1px solid #ff4d4d', borderRadius: '12px', fontWeight: 'bold', cursor: guardando ? 'not-allowed' : 'pointer', textTransform: 'uppercase', fontSize: '1rem', opacity: guardando ? 0.6 : 1 }}>
                                ❌ CANCELAR
                            </button>
                        </div>
                    )}
                </form>
            </div>

            <div style={{ background: 'rgba(0,0,0,0.5)', padding: '20px', borderRadius: '15px', border: '1px solid rgba(255,255,255,0.1)', textAlign: 'center' }}>
                <p style={{ color: '#888', fontSize: '0.9rem', margin: 0 }}>
                    💡 Los campos marcados con * son obligatorios. El rol no puede ser modificado.
                </p>
            </div>
        </div>
    );
}