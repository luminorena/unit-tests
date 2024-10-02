package ru.otus.bank.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.bank.dao.AccountDao;
import ru.otus.bank.entity.Account;
import ru.otus.bank.entity.Agreement;
import ru.otus.bank.service.exception.AccountException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {
    @Mock
    AccountDao accountDao;

    @InjectMocks
    AccountServiceImpl accountServiceImpl;


    @Test
    public void testTransfer() {
        Account sourceAccount = new Account();
        sourceAccount.setAmount(new BigDecimal(100));

        Account destinationAccount = new Account();
        destinationAccount.setAmount(new BigDecimal(10));

        when(accountDao.findById(eq(1L))).thenReturn(Optional.of(sourceAccount));
        when(accountDao.findById(eq(2L))).thenReturn(Optional.of(destinationAccount));

        accountServiceImpl.makeTransfer(1L, 2L, new BigDecimal(10));

        assertEquals(new BigDecimal(90), sourceAccount.getAmount());
        assertEquals(new BigDecimal(20), destinationAccount.getAmount());
    }

    @Test
    public void testSourceNotFound() {
        when(accountDao.findById(any())).thenReturn(Optional.empty());

        AccountException result = assertThrows(AccountException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                accountServiceImpl.makeTransfer(1L, 2L, new BigDecimal(10));
            }
        });
        assertEquals("No source account", result.getLocalizedMessage());
    }


    @Test
    public void testTransferWithVerify() {
        Account sourceAccount = new Account();
        sourceAccount.setAmount(new BigDecimal(100));
        sourceAccount.setId(1L);

        Account destinationAccount = new Account();
        destinationAccount.setAmount(new BigDecimal(10));
        destinationAccount.setId(2L);

        when(accountDao.findById(eq(1L))).thenReturn(Optional.of(sourceAccount));
        when(accountDao.findById(eq(2L))).thenReturn(Optional.of(destinationAccount));

        ArgumentMatcher<Account> sourceMatcher =
                argument -> argument.getId().equals(1L) && argument.getAmount().equals(new BigDecimal(90));

        ArgumentMatcher<Account> destinationMatcher =
                argument -> argument.getId().equals(2L) && argument.getAmount().equals(new BigDecimal(20));

        accountServiceImpl.makeTransfer(1L, 2L, new BigDecimal(10));
// проверяет, какие методы были вызываны
        verify(accountDao).save(argThat(sourceMatcher));
        verify(accountDao).save(argThat(destinationMatcher));
        }



        @Test
        void getAllAccountsTest(){
            Account account1 = new Account();
            account1.setId(1L);
            account1.setNumber("1");
            account1.setType(1);
            account1.setAmount(new BigDecimal(1L));
            account1.setAgreementId(1L);

            Account account2 = new Account();
            account2.setId(1L);
            account2.setNumber("1");
            account2.setType(1);
            account2.setAmount(new BigDecimal(1L));
            account2.setAgreementId(1L);

            List<Account> expectedAccounts = Arrays.asList(account1, account2);
            when(accountDao.findAll()).thenReturn(expectedAccounts);
            List<Account> actualAccounts = accountServiceImpl.getAccounts();
            assertEquals(expectedAccounts, actualAccounts);

        }


        @Test
        void getAccountsByIdTest(){
            Account account1 = new Account();
            Account account2 = new Account();
            Agreement agreement = new Agreement();
            List<Account> expectedAccounts = Arrays.asList(account1, account2);


            when(accountDao.findByAgreementId(agreement.getId()))
                    .thenReturn(expectedAccounts);

            List<Account> actualAccounts = accountServiceImpl.getAccounts(agreement);

            assertEquals(expectedAccounts, actualAccounts);
        }

        @Test
        void addAccountTest(){
            Account account = new Account();
            account.setAgreementId(1L);
            account.setAgreementId(1L);
            account.setType(1);
            account.setNumber("12");
            account.setId(23L);
            Agreement agreement = new Agreement();
            agreement.setId(1L);
            accountServiceImpl.addAccount(agreement,"12",
                    1, new BigDecimal(1));
            ArgumentMatcher<Account> matcher =
                    argument ->  argument.getAgreementId().equals(1L)
                            && argument.getNumber().equals("12") && argument.getType().equals(1)
                    && agreement.getId().equals(1L);
            verify(accountDao).save(argThat(matcher));
        }

        @Test
        void chargeTest(){
           Account account = new Account();
           account.setId(1L);
           account.setAmount(new BigDecimal(1));
           when(accountDao.findById(1L)).thenReturn(Optional.of(account));

            boolean result = accountServiceImpl.charge(1L, new BigDecimal(1));
            account.setAmount(account.getAmount().subtract(new BigDecimal(1)));

            assertTrue(result);

            verify(accountDao).save(account);
        }


}
