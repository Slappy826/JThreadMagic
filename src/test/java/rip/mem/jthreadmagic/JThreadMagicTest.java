package rip.mem.jthreadmagic;

public class JThreadMagicTest {

    public static void main(String[] args) {
        boolean[] data = new boolean[] {
            false, //whether loop has been entered
            false  //whether threaddeath was caught
        };

        Thread runner = new Thread(() -> {
            try{
                data[0] = true;
                while(true){}
            }catch(ThreadDeath ignored){
                data[1] = true;
            }
        }, "Runner Thread");
        runner.start();

        long startTime = System.currentTimeMillis();
        while(!data[0]){
            if(System.currentTimeMillis() - startTime > 1000)
                throw new AssertionError("Loop was not entered");

            Thread.onSpinWait();
        }

        JThreadMagic.stopThread(runner);
        while(!data[1]){
            if(System.currentTimeMillis() - startTime > 1000)
                throw new AssertionError("ThreadDeath was not caught and thread is " + (runner.isAlive() ? "still" : "not") + " running");

            Thread.onSpinWait();
        }

        System.out.println("Test passed!");
    }

}