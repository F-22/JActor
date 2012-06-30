package org.agilewiki.jactor.lpc.timingTest;

import junit.framework.TestCase;
import org.agilewiki.jactor.*;

/**
 * Test code.
 */
public class MailboxTest extends TestCase {
    public void testTiming() {
        int c = 1;
        int b = 1;
        int p = 1;
        int t = 1;

        //int c = 50000000;
        //int b = 1;
        //int p = 1;
        //int t = 1;

        //burst size of 1
        //1 parallel runs of 100000000 messages each.
        //100000000 messages sent with 1 threads.
        //msgs per sec = 47,236,655
        //21 nanoseconds per message

        //int c = 50000;
        //int b = 1000;
        //int p = 16;
        //int t = 8;

        //burst size of 1000
        //16 parallel runs of 100,000,000 messages each.
        //320000000 messages sent with 8 threads.
        //msgs per sec = 107,044,891
        //9.3 nanoseconds per message

        MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(t);
        try {
            Actor[] senders = new Actor[p];
            int i = 0;
            while (i < p) {
                Mailbox echoMailbox = mailboxFactory.createMailbox();
                Echo echo = new Echo();
                echo.initialize(echoMailbox);
                echo.setInitialBufferCapacity(b + 10);
                Mailbox senderMailbox = mailboxFactory.createAsyncMailbox();
                if (b == 1) {
                    Sender1 s = new Sender1(echo, c, b);
                    s.initialize(senderMailbox);
                    senders[i] = s;
                } else {
                    Sender s = new Sender(echo, c, b);
                    s.initialize(senderMailbox);
                    senders[i] = s;
                }
                senders[i].setInitialBufferCapacity(b + 10);
                i += 1;
            }
            JAParallel parallel = new JAParallel();
            parallel.initialize(mailboxFactory.createMailbox());
            parallel.actors = senders;
            JAFuture future = new JAFuture();
            RealRequest.req.send(future, parallel);
            RealRequest.req.send(future, parallel);
            long t0 = System.currentTimeMillis();
            RealRequest.req.send(future, parallel);
            long t1 = System.currentTimeMillis();
            SimpleRequest.req.send(future, parallel);
            long t2 = System.currentTimeMillis();
            System.out.println("null test time " + (t2 - t1));
            System.out.println("" + p + " parallel runs of " + (2L * c * b) + " messages each.");
            System.out.println("" + (2L * c * b * p) + " messages sent with " + t + " threads.");
            if (t1 != t0 && t1 - t0 - t2 + t1 > 0) {
                System.out.println("msgs per sec = " + ((2L * c * b * p) * 1000L / (t1 - t0)));
                System.out.println("adjusted msgs per sec = " + ((2L * c * b * p) * 1000L / (t1 - t0 - t2 + t1)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mailboxFactory.close();
        }
    }
}
