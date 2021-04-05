package net.shyshkin.study.grpc.grpcintro.server.rpctypes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountDatabaseTest {

    private AccountDatabase accountDatabase;

    @BeforeEach
    void setUp() {
        accountDatabase = new AccountDatabase();
    }

    @Test
    void getWhenPresent() {
        //given
        int accountId = 3;

        //when
        int balance = accountDatabase.getBalance(accountId);

        //then
        assertEquals(accountId * 111, balance);
    }

    @Test
    void getWhenAbsent() {
        //given
        int accountId = 1_000_000;

        //when
        Executable exec = () -> {
            int balance = accountDatabase.getBalance(accountId);
        };

        //then
        assertThrows(NullPointerException.class, exec);
    }

    @Nested
    class AddBalance {

        @Test
        void onceWhenAbsent() {
            //given
            int accountId = -1;
            int amountToAdd = 321;

            //when
            Executable exec = () -> {
                int balance = accountDatabase.addBalance(accountId, amountToAdd);
            };

            //then
            assertThrows(NullPointerException.class, exec);
        }

        @Test
        void onceWhenPresent() {
            //given
            int accountId = 3;
            int amountToAdd = 321;

            //when
            int balance = accountDatabase.addBalance(accountId, amountToAdd);

            //then
            assertEquals(accountId * 111 + amountToAdd, balance);
        }

        @Test
        void multipleWhenPresent() {
            //given
            int accountId = 3;
            int amountToAdd = 321;
            int count = 5;

            //when
            int balance = 0;
            for (int i = 0; i < count; i++) {
                balance = accountDatabase.addBalance(accountId, amountToAdd);
            }

            //then
            assertEquals(accountId * 111 + amountToAdd * count, balance);
        }
    }

    @Nested
    class DeductBalance {

        @Test
        void onceWhenAbsent() {
            //given
            int accountId = -1;
            int amount = 321;

            //when
            Executable exec = () -> {
                int balance = accountDatabase.deductBalance(accountId, amount);
            };

            //then
            assertThrows(NullPointerException.class, exec);
        }

        @Test
        void onceWhenPresent() {
            //given
            int accountId = 33;
            int amount = 321;

            //when
            int balance = accountDatabase.deductBalance(accountId, amount);

            //then
            assertEquals(accountId * 111 - amount, balance);
        }

        @Test
        void multipleWhenPresent() {
            //given
            int accountId = 33;
            int amount = 321;
            int count = 5;

            //when
            int balance = 0;
            for (int i = 0; i < count; i++) {
                balance = accountDatabase.deductBalance(accountId, amount);
            }

            //then
            assertEquals(accountId * 111 - amount * count, balance);
        }
    }

}