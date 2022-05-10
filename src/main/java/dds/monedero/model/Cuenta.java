package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cuenta {

  private double saldo;
  private List<Movimiento> movimientos = new ArrayList<>();
  private final int depositosMaximos = 3;
  private final double limiteDiario = 1000;

  public Cuenta() {
    saldo = 0;
  }

  public Cuenta(double montoInicial) {
    saldo = montoInicial;
  }

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }

  public void poner(double cuanto) {
    if (!esMontoValido(cuanto)) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }

    if (seSuperoLaCantidadDeDepositosMaximo()) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + this.depositosMaximos + " depositos diarios");
    }

    ponerSaldo(cuanto);
    agregarMovimiento(new Movimiento(LocalDate.now(), cuanto, true));
  }

  public void sacar(double cuanto) {
    if (!esMontoValido(cuanto)) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }

    if (!sePuedeExtraerMonto(cuanto)) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }

    if (cuanto > limiteDisponibleDe(LocalDate.now())) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + this.limiteDiario
          + " diarios, límite: " + limiteDisponibleDe(LocalDate.now()));
    }

    sacarSaldo(cuanto);
    agregarMovimiento(new Movimiento(LocalDate.now(), cuanto, false));
  }

  public void agregarMovimiento(Movimiento movimiento) {
    movimientos.add(movimiento);
  }

  public void ponerSaldo(double monto) {
    this.saldo += monto;
  }

  public void sacarSaldo(double monto) {
    this.saldo -= monto;
  }

  public double getMontoExtraidoA(LocalDate fecha) {
    return getMovimientos().stream()
        .filter(movimiento -> !movimiento.isDeposito() && movimiento.getFecha().equals(fecha))
        .mapToDouble(Movimiento::getMonto)
        .sum();
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

  public int cantidadDeMovimientos() {
    return movimientos.size();
  }

  public boolean esMontoValido(double monto) {
    return monto > 0;
  }

  public boolean seSuperoLaCantidadDeDepositosMaximo() {
    return getMovimientos().stream().filter(Movimiento::isDeposito).count() >= this.depositosMaximos;
  }

  public boolean sePuedeExtraerMonto(double monto) {
    return getSaldo() - monto > 0;
  }

  public double limiteDisponibleDe(LocalDate fecha) {
      return this.limiteDiario - getMontoExtraidoA(fecha);
  }
}
