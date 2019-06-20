@echo off
set /P confirm="Are you sure you want to remake all resources? (Y/N): "
if "%confirm%"=="Y" (
	for %%f in ("make*.bat") do (
		if not "%%f"=="makeresources.bat" if not "%%f"=="makeallresources.bat" (
			echo %%f:
			call %%f %*
		)
	)
) else echo Operation cancelled.