package ru.otus.bank.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.bank.dao.AccountDao;
import ru.otus.bank.entity.Account;
import ru.otus.bank.entity.Agreement;
import ru.otus.bank.service.exception.AccountException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
            Account account = new Account();
            AccountServiceImpl acc = Mockito.mock(AccountServiceImpl.class);
            lenient().when(acc.getAccounts().iterator()).thenCallRealMethod();
            lenient().when(accountDao.findAll()).thenReturn(List.of(account));
        }

        @Test
        void getAccountsByIdTest(){
            Agreement agreement = new Agreement();
            List<Account> accountList = new ArrayList<>();
            lenient().when(accountServiceImpl.getAccounts(agreement)).thenReturn(accountList);
            lenient().when(accountDao.findByAgreementId(agreement.getId())).thenReturn(accountList);
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
            account.setAmount(new BigDecimal(12));
            account.setNumber("23");
            account.setType(32);
            account.setAgreementId(1L);
            lenient().when(accountDao.findById(1L)).thenReturn(Optional.of(account));
            lenient().when(accountDao.findById(null)).thenThrow(AccountException.class);
            accountServiceImpl.charge(1L, new BigDecimal(1));
            verify(accountDao).save(account);

        }


}
