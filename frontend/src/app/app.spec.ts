import { TestBed } from '@angular/core/testing';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
    }).compileComponents();
  });

  it('se monte sans erreur', () => {
    // Arrange + Act
    const fixture = TestBed.createComponent(App);

    // Assert
    expect(fixture.componentInstance).toBeTruthy();
  });

  it("affiche le nom de l'application dans la barre supérieure", async () => {
    // Arrange + Act
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();

    // Assert
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('mat-toolbar span')?.textContent?.trim()).toBe(
      'MédiLabo Solutions',
    );
  });
});
