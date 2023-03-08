package example;

import ev3dev.actuators.lego.motors.EV3LargeRegulatedMotor;
import ev3dev.actuators.lego.motors.EV3MediumRegulatedMotor;
import ev3dev.sensors.Battery;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.internals.EV3DevPort;
import lejos.utility.Delay;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class test_af_tcp {

    public static void main(final String[] args) throws IOException {

        System.out.println("Foer");
        //givenGreetingClient_whenServerRespondsWhenStarted_thenCorrect();
        GreetClient client = new GreetClient();
        client.startConnection("192.168.29.191", 6666);
        System.out.println("Efter start conn");
        final EV3LargeRegulatedMotor motorLeft = new EV3LargeRegulatedMotor(MotorPort.A);
        final EV3LargeRegulatedMotor motorRight = new EV3LargeRegulatedMotor(MotorPort.B);
        final EV3MediumRegulatedMotor hatch = new EV3MediumRegulatedMotor(MotorPort.C);

        final int motorSpeed = 100 * (int) Battery.getInstance().getVoltage();
        motorLeft.setSpeed(motorSpeed);
        motorRight.setSpeed(motorSpeed);
        hatch.setSpeed(180);
        String response;
        do {
            response = client.sendMessage("Got it");
            if (response.equals("GO")) {
                motorLeft.forward();
                motorRight.forward();
                Delay.msDelay(500);
                motorLeft.stop();
                motorRight.stop();

            }
            System.out.println(response);
        } while (!response.equals("exit"));
        /*
        System.out.println("Creating Motor A & B & C");
        final EV3LargeRegulatedMotor motorLeft = new EV3LargeRegulatedMotor(MotorPort.A);
        final EV3LargeRegulatedMotor motorRight = new EV3LargeRegulatedMotor(MotorPort.B);
        final EV3MediumRegulatedMotor hatch = new EV3MediumRegulatedMotor(MotorPort.C);

        //To Stop the motor in case of pkill java for example
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                System.out.println("Emergency Stop");
                motorLeft.stop();
                motorRight.stop();
            }
        }));


        System.out.println("Defining the Stop mode");
        motorLeft.brake();
        motorRight.brake();

        System.out.println("Defining motor speed");
        final int motorSpeed = 100 * (int) Battery.getInstance().getVoltage();
        motorLeft.setSpeed(motorSpeed);
        motorRight.setSpeed(motorSpeed);
        hatch.setSpeed(180);


        System.out.println("Go Forward with the motors");
        //motorLeft.forward();
        //motorRight.forward();
        hatch.backward();

        Delay.msDelay(500);

        hatch.stop();
        motorLeft.forward();
        motorRight.forward();

        Delay.msDelay(1800);
        System.out.println("Stop motors");
        motorLeft.stop();
        motorRight.stop();
        hatch.stop();

        System.out.println("Go Backward with the motors");
        motorLeft.backward();
        motorRight.backward();
        //hatch.backward();

        Delay.msDelay(200);

        System.out.println("Stop motors");
        motorLeft.stop();
        motorRight.stop();
        hatch.stop();

        System.out.println("Checking Battery");
        System.out.println("Votage: " + Battery.getInstance().getVoltage());
*/
        System.exit(0);
    }
}

