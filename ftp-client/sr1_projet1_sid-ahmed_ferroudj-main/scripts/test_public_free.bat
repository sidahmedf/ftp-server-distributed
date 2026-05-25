@echo off
echo
echo   TEST SUR SERVEUR PUBLIC (ftp.free.fr)
echo ===============================================

echo [TEST 1] Arborescence Simple (Profondeur 1)
REM Se connecte à ftp.free.fr sur le port 21 en tant qu'anonyme
java -cp ..\bin fr.univlille.sr1.treeftp.Main ftp.free.fr 21 anonymous guest -depth 1
echo.

echo [TEST 2] Sortie JSON (Dossiers uniquement, Profondeur 1)
REM Récupère uniquement les dossiers au format JSON
java -cp ..\bin fr.univlille.sr1.treeftp.Main ftp.free.fr 21 anonymous guest -json -d -depth 1
echo.

echo [TEST 3] Mode BFS (Avec Permissions)
REM Utilise le parcours en largeur (Breadth-First) pour afficher les permissions
java -cp ..\bin fr.univlille.sr1.treeftp.Main ftp.free.fr 21 anonymous guest -bfs -p -depth 1
echo.

echo ===============================================
echo   TESTS TERMINÉS
echo ===============================================
pause