const { test, expect } = require('@playwright/test');
const { faker } = require('@faker-js/faker');

let userData;

test.describe('Fluxo de cadastro de usuário', () => {
  test.beforeAll(() => {
    userData = {
      email: faker.internet.email(),
      password: faker.internet.password(),
      fullName: faker.person.fullName(),
      username: faker.internet.userName().toLowerCase()
    };
  });

  test('Cadastro de novo usuário com sucesso', async ({ page }) => {
    await page.goto('http://localhost:3000/signup');

    await page.fill('input[name="email"]', userData.email);
    await page.fill('input[name="password"]', userData.password);
    await page.fill('input[name="fullName"]', userData.fullName);
    await page.fill('input[name="username"]', userData.username);

    await page.click('button[type="submit"]');

    await expect(page.locator('body')).toContainText(/User created successfully/i);
    await expect(page).toHaveURL('http://localhost:3000/');
  });

  test('Cadastro com email já em uso exibe mensagem de erro', async ({ page }) => {
    await page.goto('http://localhost:3000/signup');

    await page.fill('input[name="email"]', userData.email); 
    await page.fill('input[name="password"]', faker.internet.password());
    await page.fill('input[name="fullName"]', faker.person.fullName());
    await page.fill('input[name="username"]', faker.internet.userName().toLowerCase());

    await page.click('button[type="submit"]');

    await expect(page.locator('body')).toContainText(/email already in use/i);
  });

  test('Cadastro com username já em uso exibe mensagem de erro', async ({ page }) => {
    await page.goto('http://localhost:3000/signup');

    await page.fill('input[name="email"]', faker.internet.email());
    await page.fill('input[name="password"]', faker.internet.password());
    await page.fill('input[name="fullName"]', faker.person.fullName());
    await page.fill('input[name="username"]', userData.username); 

    await page.click('button[type="submit"]');

    await expect(page.locator('body')).toContainText(/Username already in use/i);
  });

});
