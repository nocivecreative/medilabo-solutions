import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { RiskService } from './risk.service';
import { RiskReport } from './risk.model';

describe('RiskService', () => {
  let service: RiskService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(RiskService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  // Échoue si une requête attendue n'est jamais partie, ou si une requête
  // inattendue a été émise : le contrat réseau est vérifié dans les deux sens.
  afterEach(() => httpMock.verify());

  it('interroge le rapport du patient sur /api/risk/patient/{patId} en GET', () => {
    // Arrange
    const expected: RiskReport = {
      patId: 4,
      riskLevel: 'EARLY_ONSET',
      age: 24,
      triggerCount: 7,
      triggersFound: ['Hémoglobine A1C', 'Microalbumine'],
    };
    let received: RiskReport | undefined;

    // Act
    service.getByPatient(4).subscribe((report) => (received = report));
    const request = httpMock.expectOne('/api/risk/patient/4');
    request.flush(expected);

    // Assert
    expect(request.request.method).toBe('GET');
    expect(received).toEqual(expected);
  });

  it("n'émet aucune requête tant que l'Observable n'est pas souscrit", () => {
    // Arrange + Act : un Observable HttpClient est froid — sans subscribe(),
    // rien ne part sur le réseau.
    service.getByPatient(1);

    // Assert
    httpMock.expectNone('/api/risk/patient/1');
  });
});
