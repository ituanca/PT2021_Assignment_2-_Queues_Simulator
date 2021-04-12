package model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable{

    private int queueIndex;
    private BlockingQueue<Client> clients;
    private AtomicInteger waitingPeriod;
    private AtomicInteger noOfClients;
    private Thread queueThread;
    public static final String OPEN = "Open";
    public static final String CLOSED = "Closed";
    public String status;

    public Server(int queueIndex) {
        this.clients = new LinkedBlockingDeque<>();
        this.queueIndex = queueIndex;
        this.queueThread = new Thread(this);
        this.noOfClients = new AtomicInteger(0);
        this.waitingPeriod = new AtomicInteger(0);
        this.status = CLOSED;
    }

    public void addClientToQueue(Client client){
        this.clients.add(client);
        client.setStatus(Client.WAITING);
        this.waitingPeriod.addAndGet(client.getServiceTime());
        this.noOfClients.incrementAndGet();
        if(this.status.equals(CLOSED)){
            this.status = OPEN;
        }
    }

    public void removeClientFromQueue(Client client){
        this.clients.remove(client);
        client.setStatus(Client.SERVED);
        if(this.noOfClients.decrementAndGet() == 0){
            this.status = CLOSED;
        }
    }

    public void serveClient(Client client) throws InterruptedException {
        if(client.getServiceTime() > 1){
            client.setStatus(Client.BEING_SERVED);
            client.setServiceTime(client.getServiceTime() - 1);
        }else{
            client.setServiceTime(0);
            removeClientFromQueue(client);
            this.waitingPeriod.decrementAndGet();
            Thread.sleep(1000);
        }
    }

    @Override
    public void run() {
        while(status.equals(OPEN) && clients.size() > 0) {
            Client client = this.clients.peek();
            try {
                if(client != null){
                    serveClient(client);
                    this.waitingPeriod.decrementAndGet();
                    this.queueThread.wait(client.getServiceTime() * 1000);
                }else{

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Interruption occurred.");
                return;
            }
        }
    }

    public BlockingQueue<Client> getClients() {
        return clients;
    }

    public AtomicInteger getWaitingPeriod() { return waitingPeriod; }

    public void setWaitingPeriod(AtomicInteger waitingPeriod) { this.waitingPeriod = waitingPeriod; }

    public int getQueueIndex() { return queueIndex; }

    public void setQueueIndex(int queueIndex) { this.queueIndex = queueIndex; }

    public AtomicInteger getNoOfClients() { return noOfClients; }

    public void setNoOfClients(AtomicInteger noOfClients) { this.noOfClients = noOfClients; }

    public Thread getQueueThread() { return queueThread; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}
