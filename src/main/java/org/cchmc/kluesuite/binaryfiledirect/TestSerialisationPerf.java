package org.cchmc.kluesuite.binaryfiledirect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

//public final class TestSerialisationPerf
//{
//    public static final int REPETITIONS = 1 * 1000 * 1000;
//
//    private static ObjectToBeSerialised ITEM =
//            new ObjectToBeSerialised(
//                    1010L, true, 777, 99,
//                    new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0},
//                    new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
//
//
//    public static void main(final String[] arg) throws Exception
//    {
//        for (final PerformanceTestCase testCase : testCases)
//        {
//            for (int i = 0; i < 5; i++)
//            {
//                testCase.performTest();
//
//                System.out.format("%d %s\twrite=%,dns read=%,dns total=%,dns\n",
//                        i,
//                        testCase.getName(),
//                        testCase.getWriteTimeNanos(),
//                        testCase.getReadTimeNanos(),
//                        testCase.getWriteTimeNanos() +
//                                testCase.getReadTimeNanos());
//
//                if (!ITEM.equals(testCase.getTestOutput()))
//                {
//                    throw new IllegalStateException("Objects do not match");
//                }
//
//                System.gc();
//                Thread.sleep(3000);
//            }
//        }
//    }
//
//    private static final PerformanceTestCase[] testCases =
//            {
//                    new PerformanceTestCase("Serialisation", REPETITIONS, ITEM)
//                    {
//                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//                        public void testWrite(ObjectToBeSerialised item) throws Exception
//                        {
//                            for (int i = 0; i < REPETITIONS; i++)
//                            {
//                                baos.reset();
//
//                                ObjectOutputStream oos = new ObjectOutputStream(baos);
//                                oos.writeObject(item);
//                                oos.close();
//                            }
//                        }
//
//                        public ObjectToBeSerialised testRead() throws Exception
//                        {
//                            ObjectToBeSerialised object = null;
//                            for (int i = 0; i < REPETITIONS; i++)
//                            {
//                                ByteArrayInputStream bais =
//                                        new ByteArrayInputStream(baos.toByteArray());
//                                ObjectInputStream ois = new ObjectInputStream(bais);
//                                object = (ObjectToBeSerialised)ois.readObject();
//                            }
//
//                            return object;
//                        }
//                    },
//
//                    new PerformanceTestCase("ByteBuffer", REPETITIONS, ITEM)
//                    {
//                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
//
//                        public void testWrite(ObjectToBeSerialised item) throws Exception
//                        {
//                            for (int i = 0; i < REPETITIONS; i++)
//                            {
//                                byteBuffer.clear();
//                                item.write(byteBuffer);
//                            }
//                        }
//
//                        public ObjectToBeSerialised testRead() throws Exception
//                        {
//                            ObjectToBeSerialised object = null;
//                            for (int i = 0; i < REPETITIONS; i++)
//                            {
//                                byteBuffer.flip();
//                                object = ObjectToBeSerialised.read(byteBuffer);
//                            }
//
//                            return object;
//                        }
//                    },
//
//                    new PerformanceTestCase("UnsafeMemory", REPETITIONS, ITEM)
//                    {
//                        UnsafeMemory buffer = new UnsafeMemory(new byte[1024]);
//
//                        public void testWrite(ObjectToBeSerialised item) throws Exception
//                        {
//                            for (int i = 0; i < REPETITIONS; i++)
//                            {
//                                buffer.reset();
//                                item.write(buffer);
//                            }
//                        }
//
//                        public ObjectToBeSerialised testRead() throws Exception
//                        {
//                            ObjectToBeSerialised object = null;
//                            for (int i = 0; i < REPETITIONS; i++)
//                            {
//                                buffer.reset();
//                                object = ObjectToBeSerialised.read(buffer);
//                            }
//
//                            return object;
//                        }
//                    },
//            };
//}
