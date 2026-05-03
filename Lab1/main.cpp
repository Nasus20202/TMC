#include <iostream>
#include <iomanip>
#include "gdal_priv.h"
#include "cpl_conv.h"

int main()
{
	GDALAllRegister();
	GDALDataset *dataset;

	dataset = (GDALDataset *)GDALOpen("data/utm.tif", GA_ReadOnly);
	if (dataset == NULL)
	{
		std::cerr << "Failed to open the file" << std::endl;
		return 1;
	}

	const char *filename = dataset->GetDescription();
	const int width = dataset->GetRasterXSize(), height = dataset->GetRasterYSize();
	const int channelCount = dataset->GetRasterCount();
	const char *projection = dataset->GetProjectionRef();
	double transformation[6];
	const CPLErr transformationError = dataset->GetGeoTransform(transformation);
	const char *driverName = dataset->GetDriver()->GetDescription();

	std::cout << std::fixed << std::setprecision(2);
	std::cout << "File name: " << filename << std::endl;
	std::cout << "Size: " << width << "x" << height << std::endl;
	std::cout << "Channels: " << channelCount << std::endl;
	std::cout << "Projection: " << projection << std::endl;
	if (transformationError == CE_None)
	{
		std::cout << "Transformation: ";
		for (int i = 0; i < (int)(sizeof(transformation) / sizeof(transformation[0])); i++)
		{
			std::cout << "[" << i << "] = " << transformation[i] << " ";
		}
		std::cout << std::endl;
	}
	else
	{
		std::cout << "Transformation: Not found" << std::endl;
	}
	std::cout << "Driver: " << driverName << std::endl;

	bool fromProjectionSpace;
	std::cout << "Do you want to input the point in the projection or pixel space? (1 for projection, 0 for pixel): ";
	std::cin >> fromProjectionSpace;

	double x, y;
	if (fromProjectionSpace)
	{
		double xProjection, yProjection;
		// 440720 3751320 - (0, 0)
		// 440720 3720660 - (0, 511)
		// 471380 3751320 - (511, 0)
		// 471380 3720660 - (511, 511)
		std::cout << "Input point in the projection space (Xp, Yp): ";
		std::cin >> xProjection >> yProjection;

		/*
		 * Xp = 0 + P * 1 + L * 2
		 * Yp = 3 + P * 4 + L * 5
		 *
		 * Xp - 0 = P * 1 + L * 2
		 * Yp - 3 = P * 4 + L * 5
		 *
		 * [1, 2] [P] = [Xp - 0]
		 * [4, 5] [L]   [Yp - 3]
		 *
		 * [P] = [1, 2]^-1 [Xp - 0]
		 * [L]   [4, 5]    [Yp - 3]
		 *       det = 1*5 - 2*4
		 *
		 * [P] = 1/(1*5 - 2*4) [5, -2] [Xp - 0]
		 * [L]                 [-4, 1] [Yp - 3]
		 *
		 * P = (5 * (Xp - 0) - 2 * (Yp - 3)) / (1*5 - 2*4)
		 * L = (-4 * (Xp - 0) + 1 * (Yp - 3)) / (1*5 - 2*4)
		 */

		const double det = transformation[1] * transformation[5] - transformation[2] * transformation[4];
		const int p = (transformation[5] * (xProjection - transformation[0]) - transformation[2] * (yProjection - transformation[3])) / det;
		const int l = (-transformation[4] * (xProjection - transformation[0]) + transformation[1] * (yProjection - transformation[3])) / det;
		std::cout << "Corresponding point in the pixel space (P, L): " << p << ", " << l << std::endl;

		x = p;
		y = l;
	}
	else
	{
		int p, l;
		std::cout << "Input point in the pixel space (P, L): ";
		std::cin >> p >> l;
		x = p;
		y = l;

		double xProjection = transformation[0] + p * transformation[1] + l * transformation[2];
		double yProjection = transformation[3] + p * transformation[4] + l * transformation[5];
		std::cout << "Corresponding point in the projection space (Xp, Yp): " << xProjection << ", " << yProjection << std::endl;
	}

	GDALRasterBand *rasterBand = dataset->GetRasterBand(1);
	GDALDataType dataType = rasterBand->GetRasterDataType();
	long long int pixelValue = 0; // 4 empty bytes
	// RasterIO(
	//		GDALRWFlag eRWFlag,
	//		int nXOff,
	//		int nYOff,
	//		int nXSize,
	//		int nYSize,
	//		void *pData,
	//		int nBufXSize,
	//		int nBufYSize,
	//		GDALDataType eBufType,
	//		GSpacing nPixelSpace,
	//		GSpacing nLineSpace,
	//		GDALRasterIOExtraArg *psExtraArg
	// )
	CPLErr readError = rasterBand->RasterIO(
		GF_Read, x, y,
		1, 1,
		&pixelValue,
		1, 1,
		dataType,
		0, 0);
	if (readError != CE_None)
	{
		std::cerr << "Failed to read the pixel value" << std::endl;
		return 1;
	}
	std::cout << "Pixel value of type " << GDALGetDataTypeName(dataType) << ": ";
	switch (dataType)
	{
	case GDT_Byte:
	case GDT_Int16:
	case GDT_Int32:
	case GDT_CInt16:
	case GDT_CInt32:
		std::cout << (int)pixelValue << std::endl;
		break;
	case GDT_UInt16:
	case GDT_UInt32:
		std::cout << (unsigned int)pixelValue << std::endl;
		break;
	case GDT_Float32:
	case GDT_CFloat32:
		std::cout << (float)pixelValue << std::endl;
		break;
	case GDT_Float64:
	case GDT_CFloat64:
		std::cout << (double)pixelValue << std::endl;
		break;
	default:
		std::cout << pixelValue << std::endl;
	}

	/* Expected values:
		440750 3751290 107
		440810 3751290 123
		440870 3751290 132
		440930 3751290 115
		440990 3751290 132
		441050 3751290 132
		441110 3751290 140
		441170 3751290 132
		441230 3751290 132
		441290 3751290 132
	*/

	GDALClose(dataset);
}
