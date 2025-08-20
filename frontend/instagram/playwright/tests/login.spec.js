const { test, expect } = require('@playwright/test');
const { faker } = require('@faker-js/faker');

let userData;

test.describe('Fluxo de login de usuário', () => {
  test.beforeAll(async ({ browser }) => {
    userData = {
      email: faker.internet.email(),
      password: faker.internet.password(),
      fullName: faker.person.fullName(),
      username: faker.internet.userName().toLowerCase()
    };

    const page = await browser.newPage();
    await page.goto('http://localhost:3000/signup');

    await page.fill('input[name="email"]', userData.email);
    await page.fill('input[name="password"]', userData.password);
    await page.fill('input[name="fullName"]', userData.fullName);
    await page.fill('input[name="username"]', userData.username);

    await page.click('button[type="submit"]');
    await expect(page.locator('body')).toContainText(/User created successfully/i);
    await page.close();
  });

  test('Login com username cadastrado', async ({ page }) => {
    await page.goto('http://localhost:3000/');

    await page.fill('input[name="username"]', userData.username); 
    await page.fill('input[name="password"]', userData.password);

    await page.click('button[type="submit"]');

    await expect(page.locator('body')).toContainText(/Você está logado./i); 
    await expect(page).toHaveURL('http://localhost:3000/feed');
  });

  test('Logout após login com sucesso', async ({ page }) => {
    await page.goto('http://localhost:3000/');

    await page.fill('input[name="username"]', userData.username); 
    await page.fill('input[name="password"]', userData.password);

    await page.click('button[type="submit"]');
    await expect(page).toHaveURL('http://localhost:3000/feed');

    await page.click('text=Logout');

    const token = await page.evaluate(() => localStorage.getItem('jwtToken'));
    expect(token).toBeNull();

    await expect(page).toHaveURL('http://localhost:3000/');
  });
});
