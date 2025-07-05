package com.example.imccalculator.controller;

import com.example.imccalculator.entity.Medicion;
import com.example.imccalculator.entity.User;
import com.example.imccalculator.service.MedicionService;
import com.example.imccalculator.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MedicionController {
    
    private final UserService userService;
    private final MedicionService medicionService;
    
    @GetMapping("/nueva-medicion")
    public String nuevaMedicion(Authentication authentication, Model model) {
        String nombreUsuario = authentication.getName();
        Optional<User> userOpt = userService.buscarPorNombreUsuario(nombreUsuario);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("user", user);
        }
        
        return "nueva-medicion";
    }
    
    @PostMapping("/calcular-imc")
    @ResponseBody
    public MedicionService.IMCResult calcularIMC(@RequestParam Double peso,
                                                Authentication authentication) {
        String nombreUsuario = authentication.getName();
        Optional<User> userOpt = userService.buscarPorNombreUsuario(nombreUsuario);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return medicionService.calcularIMC(peso, user.getEstatura());
        }
        
        throw new RuntimeException("Usuario no encontrado");
    }
    
    @PostMapping("/guardar-medicion")
    public String guardarMedicion(@RequestParam Double peso,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        
        String nombreUsuario = authentication.getName();
        Optional<User> userOpt = userService.buscarPorNombreUsuario(nombreUsuario);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Guardar medición
            medicionService.guardarMedicion(user, peso);
            
            // Actualizar peso del usuario
            userService.actualizarPeso(user, peso);
            
            redirectAttributes.addFlashAttribute("message", "Medición guardada correctamente");
            return "redirect:/home";
        }
        
        return "redirect:/nueva-medicion";
    }
    
    @GetMapping("/historial")
    public String historial(Authentication authentication, Model model) {
        String nombreUsuario = authentication.getName();
        Optional<User> userOpt = userService.buscarPorNombreUsuario(nombreUsuario);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            List<Medicion> mediciones = medicionService.obtenerMedicionesPorUsuario(user);
            
            model.addAttribute("user", user);
            model.addAttribute("mediciones", mediciones);
            model.addAttribute("totalMediciones", mediciones.size());
            
            if (!mediciones.isEmpty()) {
                model.addAttribute("pesoActual", mediciones.get(0).getPeso());
                model.addAttribute("imcActual", mediciones.get(0).getImc());
            }
        }
        
        return "historial";
    }
}
